package com.example.j2ee_project.service.payment;

import com.example.j2ee_project.entity.Bill;
import com.example.j2ee_project.entity.Booking;
import com.example.j2ee_project.entity.Order;
import com.example.j2ee_project.entity.Status;
import com.example.j2ee_project.exception.ResourceNotFoundException;
import com.example.j2ee_project.model.dto.BillDTO;
import com.example.j2ee_project.model.dto.PaymentDTO;
import com.example.j2ee_project.model.dto.RefundPaymentDTO;
import com.example.j2ee_project.model.response.ResponseData;
import com.example.j2ee_project.repository.BillRepository;
import com.example.j2ee_project.repository.BookingRepository;
import com.example.j2ee_project.repository.OrderRepository;
import com.example.j2ee_project.repository.StatusRepository;
import com.example.j2ee_project.utils._enum.EStatus;
import com.example.j2ee_project.utils.vnpay.VnPayUtil;
import jakarta.annotation.PostConstruct;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class PaymentService {

    @Value("${vnp.pay.url}")
    private String vnpPayUrl;

    @Value("${vnp.tmnCode}")
    private String vnpTmnCode;

    @Value("${vnp.hashSecret}")
    private String vnpHashSecret;

    @Value("${vnp.returnUrl}")
    private String vnpReturnUrl;

    @Value("${vnp.refund.url}")
    private String vnpRefundUrl;

    private final BillRepository billRepository;
    private final BookingRepository bookingRepository;
    private final OrderRepository orderRepository;
    private final StatusRepository statusRepository;

    // In-Memory Cache để lưu hashData gốc
    private final Map<String, String> paymentCache = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public PaymentService(BillRepository billRepository, BookingRepository bookingRepository, OrderRepository orderRepository, StatusRepository statusRepository) {
        this.billRepository = billRepository;
        this.bookingRepository = bookingRepository;
        this.orderRepository = orderRepository;
        this.statusRepository = statusRepository;
    }

    @PostConstruct
    public void init() {
        // Dọn dẹp cache mỗi 5 phút
        scheduler.scheduleAtFixedRate(this::cleanupCache, 5, 5, TimeUnit.MINUTES);
    }

    public String createPaymentUrl(PaymentDTO dto) throws Exception {
        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String orderType = "100000";
        String vnp_TxnRef = dto.getBillID();
        String vnp_IpAddr = getClientIp();
        String vnp_Locale = "vn";
        String vnp_CurrCode = "VND";

        Map<String, String> vnp_Params = new TreeMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnpTmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(dto.getAmount() * 100));
        vnp_Params.put("vnp_CurrCode", vnp_CurrCode);
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", dto.getOrderInfo());
        vnp_Params.put("vnp_OrderType", orderType);
        vnp_Params.put("vnp_Locale", vnp_Locale);
        vnp_Params.put("vnp_ReturnUrl", vnpReturnUrl);
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        // Tạo query string và secure hash
        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        for (String fieldName : fieldNames) {
            String fieldValue = vnp_Params.get(fieldName);
            if (fieldValue != null && fieldValue.length() > 0) {
                // Build hashData
                if (hashData.length() > 0) {
                    hashData.append('&');
                }
                hashData.append(fieldName).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8.toString()));

                // Build query
                if (query.length() > 0) {
                    query.append('&');
                }
                query.append(fieldName).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8.toString()));
            }
        }

        System.out.println("HashData: " + hashData);
        String vnp_SecureHash = VnPayUtil.hmacSHA512(vnpHashSecret, hashData.toString());
        query.append("&vnp_SecureHash=").append(vnp_SecureHash);

        // LƯU HASHDATA GỐC VÀO CACHE
        paymentCache.put(vnp_TxnRef, hashData.toString());
        System.out.println("Saved hashData to cache for billID: " + vnp_TxnRef);
        System.out.println("Cache size: " + paymentCache.size());

        System.out.println("Generated payment URL: " + (vnpPayUrl + "?" + query));
        return vnpPayUrl + "?" + query;
    }

    public ResponseData handleCallback(Map<String, String> params) {
        try {
            System.out.println("=== VNPAY CALLBACK PARAMS ===");
            params.forEach((key, value) -> System.out.println(key + ": " + value));

            String vnp_SecureHash = params.get("vnp_SecureHash");
            String billID = params.get("vnp_TxnRef");

            if (billID == null || vnp_SecureHash == null) {
                return createErrorResponse("Thiếu thông tin billID hoặc chữ ký");
            }

            // LẤY HASHDATA GỐC TỪ CACHE
            String originalHashData = paymentCache.get(billID);

            if (originalHashData == null) {
                System.out.println("Không tìm thấy hashData trong cache cho billID: " + billID);
                return createErrorResponse("Không tìm thấy thông tin thanh toán hoặc đã hết hạn");
            }

            System.out.println("Retrieved original hashData from cache: " + originalHashData);

            // SỬ DỤNG HASHDATA GỐC ĐỂ VERIFY CHỮ KÝ
            String calculatedSignValue = VnPayUtil.hmacSHA512(vnpHashSecret, originalHashData);

            System.out.println("=== SIGNATURE VERIFICATION ===");
            System.out.println("Original HashData: " + originalHashData);
            System.out.println("Calculated Signature: " + calculatedSignValue);
            System.out.println("Received Signature: " + vnp_SecureHash);
            System.out.println("Signature Match: " + calculatedSignValue.equals(vnp_SecureHash));

            if (calculatedSignValue.equals(vnp_SecureHash)) {
                // XÓA KHỎI CACHE SAU KHI XÁC THỰC THÀNH CÔNG
                paymentCache.remove(billID);
                System.out.println("Removed billID from cache: " + billID);

                String transactionStatus = params.get("vnp_TransactionStatus");
                return updateBillAfterPayment(Integer.parseInt(billID), transactionStatus, params);
            } else {
                // Fallback: thử verify với params nhận được
                System.out.println("Trying fallback verification with received params...");
                ResponseData fallbackResult = verifyWithReceivedParams(params);
                if (fallbackResult.isSuccess()) {
                    paymentCache.remove(billID); // Xóa khỏi cache nếu fallback thành công
                }
                return fallbackResult;
            }

        } catch (Exception e) {
            System.err.println("Callback error: " + e.getMessage());
            e.printStackTrace();
            return createErrorResponse("Lỗi xử lý callback: " + e.getMessage());
        }
    }

    private ResponseData verifyWithReceivedParams(Map<String, String> params) {
        try {
            String vnp_SecureHash = params.get("vnp_SecureHash");

            // Tạo bản sao của params để tính toán chữ ký
            Map<String, String> signParams = new TreeMap<>();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                String key = entry.getKey();
                if (key.startsWith("vnp_") &&
                        !key.equals("vnp_SecureHash") &&
                        !key.equals("vnp_SecureHashType")) {
                    signParams.put(key, entry.getValue() != null ? entry.getValue() : "");
                }
            }

            StringBuilder hashData = new StringBuilder();
            for (Map.Entry<String, String> entry : signParams.entrySet()) {
                if (hashData.length() > 0) {
                    hashData.append('&');
                }
                String key = entry.getKey();
                String value = entry.getValue();
                hashData.append(key).append('=').append(URLEncoder.encode(value, StandardCharsets.UTF_8.toString()));
            }

            String calculatedSignValue = VnPayUtil.hmacSHA512(vnpHashSecret, hashData.toString());

            System.out.println("=== FALLBACK VERIFICATION ===");
            System.out.println("Fallback HashData: " + hashData.toString());
            System.out.println("Fallback Calculated Signature: " + calculatedSignValue);
            System.out.println("Fallback Received Signature: " + vnp_SecureHash);

            if (calculatedSignValue.equals(vnp_SecureHash)) {
                String transactionStatus = params.get("vnp_TransactionStatus");
                String billID = params.get("vnp_TxnRef");
                return updateBillAfterPayment(Integer.parseInt(billID), transactionStatus, params);
            } else {
                ResponseData response = new ResponseData();
                response.setMessage("Chữ ký không hợp lệ (fallback cũng thất bại)");
                response.setSuccess(false);
                response.setStatus(400);

                // Debug info
                Map<String, String> debugInfo = new HashMap<>();
                debugInfo.put("fallbackHashData", hashData.toString());
                debugInfo.put("fallbackCalculated", calculatedSignValue);
                debugInfo.put("receivedSignature", vnp_SecureHash);
                response.setData(debugInfo);

                return response;
            }
        } catch (Exception e) {
            return createErrorResponse("Lỗi fallback verification: " + e.getMessage());
        }
    }

    public ResponseData refund(RefundPaymentDTO dto) throws Exception {
        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        String vnp_TransactionDate = formatter.format(Date.from(dto.getPaymentTime().atZone(java.time.ZoneId.systemDefault()).toInstant()));

        // KIỂM TRA bắt buộc phải có transactionNo
        if (dto.getTransactionNo() == null || dto.getTransactionNo().isEmpty()) {
            throw new IllegalArgumentException("TransactionNo là bắt buộc để hoàn tiền");
        }

        Map<String, String> params = new TreeMap<>();
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "refund");
        params.put("vnp_TmnCode", vnpTmnCode);
        params.put("vnp_TransactionType", dto.getTransactionType());
        params.put("vnp_TxnRef", dto.getBillID());
        params.put("vnp_Amount", String.valueOf(dto.getAmount() * 100));
        params.put("vnp_OrderInfo", dto.getReason());
        params.put("vnp_TransactionNo", dto.getTransactionNo()); // BẮT BUỘC
        params.put("vnp_TransactionDate", vnp_TransactionDate);
        params.put("vnp_CreateDate", vnp_CreateDate);
        params.put("vnp_CreateBy", "system");
        params.put("vnp_IpAddr", getClientIp());
        params.put("vnp_RequestId", UUID.randomUUID().toString());

        // Tạo chữ ký
        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();

        for (String fieldName : fieldNames) {
            String fieldValue = params.get(fieldName);
            if (fieldValue != null && fieldValue.length() > 0) {
                if (hashData.length() > 0) {
                    hashData.append('&');
                }
                hashData.append(fieldName).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8.toString()));
            }
        }

        String vnp_SecureHash = VnPayUtil.hmacSHA512(vnpHashSecret, hashData.toString());
        params.put("vnp_SecureHash", vnp_SecureHash);

        System.out.println("=== REFUND REQUEST ===");
        System.out.println("URL: " + vnpRefundUrl);
        System.out.println("HashData: " + hashData.toString());
        System.out.println("SecureHash: " + vnp_SecureHash);
        params.forEach((key, value) -> System.out.println(key + ": " + value));

        // GỬI REQUEST dưới dạng JSON (quan trọng!)
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost post = new HttpPost(vnpRefundUrl);
        post.setHeader("Content-Type", "application/json");

        // Chuyển params sang JSON
        StringBuilder jsonBody = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (!first) jsonBody.append(",");
            jsonBody.append("\"").append(entry.getKey()).append("\":\"").append(entry.getValue()).append("\"");
            first = false;
        }
        jsonBody.append("}");

        post.setEntity(new org.apache.http.entity.StringEntity(jsonBody.toString(), StandardCharsets.UTF_8));

        ResponseData response = new ResponseData();
        try (CloseableHttpResponse httpResponse = client.execute(post);
             BufferedReader reader = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent(), StandardCharsets.UTF_8))) {

            StringBuilder responseBuilder = new StringBuilder();
            String inputLine;
            while ((inputLine = reader.readLine()) != null) {
                responseBuilder.append(inputLine);
            }

            System.out.println("=== REFUND RESPONSE ===");
            System.out.println(responseBuilder.toString());

            // Parse JSON response
            Map<String, String> responseMap = parseJsonResponse(responseBuilder.toString());
            String rspCode = responseMap.get("vnp_ResponseCode");

            if (rspCode == null) {
                System.err.println("Refund failed: vnp_ResponseCode is null. Response: " + responseBuilder.toString());
                response.setMessage("Hoàn tiền thất bại: Phản hồi từ VnPay không hợp lệ");
                response.setSuccess(false);
                response.setStatus(400);
                response.setData(responseMap);
                return response;
            }

            if ("00".equals(rspCode)) {
                response.setMessage("Hoàn tiền thành công");
                response.setSuccess(true);
                response.setData(responseMap);
                response.setStatus(200);
            } else {
                System.err.println("Refund failed with response code: " + rspCode);
                response.setMessage("Hoàn tiền thất bại: " + getVnPayErrorMessage(rspCode));
                response.setSuccess(false);
                response.setStatus(400);
                response.setData(responseMap);
            }
        } catch (Exception e) {
            System.err.println("Error during refund request: " + e.getMessage());
            e.printStackTrace();
            response.setMessage("Hoàn tiền thất bại do lỗi hệ thống: " + e.getMessage());
            response.setSuccess(false);
            response.setStatus(500);
            response.setData(null);
        } finally {
            client.close();
        }

        return response;
    }

    // Thêm method parse JSON response
    private Map<String, String> parseJsonResponse(String jsonStr) {
        Map<String, String> map = new HashMap<>();
        try {
            jsonStr = jsonStr.trim();
            if (jsonStr.startsWith("{") && jsonStr.endsWith("}")) {
                jsonStr = jsonStr.substring(1, jsonStr.length() - 1);
                for (String pair : jsonStr.split(",")) {
                    String[] keyValue = pair.split(":", 2);
                       if (keyValue.length == 2) {
                        String key = keyValue[0].trim().replace("\"", "");
                        String value = keyValue[1].trim().replace("\"", "");
                        map.put(key, value);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing JSON: " + e.getMessage());
        }
        return map;
    }
    private String getVnPayErrorMessage(String rspCode) {
        switch (rspCode) {
            case "91": return "Giao dịch không tồn tại";
            case "94": return "Giao dịch đã được hoàn tiền trước đó";
            case "99": return "Lỗi không xác định từ VnPay";
            default: return "Mã lỗi VnPay: " + rspCode;
        }
    }


    @Transactional
    public ResponseData updateBillAfterPayment(Integer billID, String transactionStatus, Map<String, String> params) {
        Bill bill = billRepository.findById(billID)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hóa đơn với ID: " + billID));

        ResponseData response = new ResponseData();

        try {
            if ("00".equals(transactionStatus)) {
                // Thanh toán thành công
                BigDecimal paidAmount = new BigDecimal(params.get("vnp_Amount")).divide(new BigDecimal(100)); // Chuyển từ VNĐ sang số tiền thực
                bill.setInitialPayment(paidAmount);
                bill.setRemainingAmount(bill.getTotalAmount().subtract(paidAmount));
                bill.setPaymentMethod("TRANSFER");
                bill.setPaymentTime(LocalDateTime.now());
                bill.setTransactionNo(params.get("vnp_TransactionNo"));

                Status successStatus = statusRepository.findByStatusName(
                                bill.getRemainingAmount().compareTo(BigDecimal.ZERO) == 0 ?
                                        EStatus.PAID.getName() : EStatus.PARTIALLY_PAID.getName())
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy trạng thái"));
                bill.setStatus(successStatus);
                billRepository.save(bill);

                // Cập nhật trạng thái booking và order
                updateRelatedEntities(bill, true);

                response.setMessage("Thanh toán hóa đơn thành công");
                response.setSuccess(true);
                response.setStatus(200);
                response.setData(mapToBillDTO(bill));
            } else {
                // Thanh toán thất bại
                Status failedStatus = statusRepository.findByStatusName(EStatus.FAILED.getName())
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy trạng thái FAILED"));
                bill.setStatus(failedStatus);
                bill.setInitialPayment(BigDecimal.ZERO);
                bill.setRemainingAmount(bill.getTotalAmount());
                bill.setPaymentTime(null);
                bill.setTransactionNo(null);
                billRepository.save(bill);

                // Cập nhật trạng thái booking
                updateRelatedEntities(bill, false);

                response.setMessage("Thanh toán hóa đơn thất bại: " + params.get("vnp_ResponseCode"));
                response.setSuccess(false);
                response.setStatus(400);
                response.setData(mapToBillDTO(bill));
            }
        } catch (Exception e) {
            response.setMessage("Lỗi khi cập nhật hóa đơn: " + e.getMessage());
            response.setSuccess(false);
            response.setStatus(500);
        }

        return response;
    }

    private void updateRelatedEntities(Bill bill, boolean isSuccess) {
        if (bill.getBooking() != null) {
            Booking booking = bill.getBooking();
            try {
                if (isSuccess) {
                    booking.setStatus(statusRepository.findByStatusName(EStatus.CONFIRMED.getName())
                            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy trạng thái CONFIRMED")));
                } else {
                    booking.setStatus(statusRepository.findByStatusName(EStatus.CANCELLED.getName())
                            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy trạng thái CANCELLED")));
                }
                bookingRepository.save(booking);
            } catch (ResourceNotFoundException e) {
                System.err.println("Lỗi khi cập nhật booking: " + e.getMessage());
            }
        }

        if (bill.getOrder() != null && isSuccess) {
            Order order = bill.getOrder();
            try {
                order.setStatus(statusRepository.findByStatusName(EStatus.PAID.getName())
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy trạng thái PAID")));
                orderRepository.save(order);
            } catch (ResourceNotFoundException e) {
                System.err.println("Lỗi khi cập nhật order: " + e.getMessage());
            }
        }
    }

    private BillDTO mapToBillDTO(Bill bill) {
        BillDTO billDTO = new BillDTO();
        billDTO.setBillID(bill.getBillID());
        billDTO.setUserID(bill.getUser() != null ? bill.getUser().getUserID() : null);
        billDTO.setUserName(bill.getUser() != null ? bill.getUser().getUsername() : null);
        billDTO.setTableID(bill.getRestaurantTable() != null ? bill.getRestaurantTable().getTableID() : null);
        billDTO.setTableName(bill.getRestaurantTable() != null ? bill.getRestaurantTable().getTableName() : null);
        billDTO.setBookingID(bill.getBooking() != null ? bill.getBooking().getBookingID() : null);
        billDTO.setOrderID(bill.getOrder() != null ? bill.getOrder().getOrderID() : null);
        billDTO.setBillDate(bill.getBillDate());
        billDTO.setInitialPayment(bill.getInitialPayment());
        billDTO.setTotalAmount(bill.getTotalAmount());
        billDTO.setRemainingAmount(bill.getRemainingAmount());
        billDTO.setPaymentMethod(bill.getPaymentMethod());
        billDTO.setPaymentTime(bill.getPaymentTime());
        billDTO.setStatusID(bill.getStatus() != null ? bill.getStatus().getStatusID() : null);
        billDTO.setTransactionNo(bill.getTransactionNo());
        billDTO.setCreatedAt(bill.getCreatedAt());
        billDTO.setUpdatedAt(bill.getUpdatedAt());
        return billDTO;
    }

    private String getClientIp() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            String remoteAddr = attributes.getRequest().getRemoteAddr();
            // Xử lý trường hợp IPv6 localhost
            if ("0:0:0:0:0:0:0:1".equals(remoteAddr) || "::1".equals(remoteAddr)) {
                return "127.0.0.1";
            }
            return remoteAddr;
        }
        return "127.0.0.1";
    }

    private Map<String, String> parseResponse(String responseStr) {
        Map<String, String> map = new HashMap<>();
        if (responseStr != null && !responseStr.isEmpty()) {
            for (String param : responseStr.split("&")) {
                String[] parts = param.split("=", 2);
                if (parts.length >= 1) {
                    String key = parts[0];
                    String value = parts.length > 1 ? parts[1] : "";
                    map.put(key, value);
                }
            }
        }
        return map;
    }

    private ResponseData createErrorResponse(String message) {
        ResponseData response = new ResponseData();
        response.setMessage(message);
        response.setSuccess(false);
        response.setStatus(400);
        return response;
    }

    private void cleanupCache() {
        System.out.println("Cleaning up payment cache. Current size: " + paymentCache.size());
        // Có thể thêm logic cleanup theo thời gian nếu cần
        // Ví dụ: xóa các entry cũ hơn 1 giờ
    }
}

















//
//package com.example.j2ee_project.service.payment;
//
//import com.example.j2ee_project.entity.Bill;
//import com.example.j2ee_project.entity.Booking;
//import com.example.j2ee_project.entity.Order;
//import com.example.j2ee_project.entity.Status;
//import com.example.j2ee_project.exception.ResourceNotFoundException;
//import com.example.j2ee_project.model.dto.BillDTO;
//import com.example.j2ee_project.model.dto.PaymentDTO;
//import com.example.j2ee_project.model.dto.RefundPaymentDTO;
//import com.example.j2ee_project.model.response.ResponseData;
//import com.example.j2ee_project.repository.BillRepository;
//import com.example.j2ee_project.repository.BookingRepository;
//import com.example.j2ee_project.repository.OrderRepository;
//import com.example.j2ee_project.repository.StatusRepository;
//import com.example.j2ee_project.utils._enum.EStatus;
//import com.example.j2ee_project.utils.vnpay.VnPayUtil;
//import org.apache.http.NameValuePair;
//import org.apache.http.client.entity.UrlEncodedFormEntity;
//import org.apache.http.client.methods.CloseableHttpResponse;
//import org.apache.http.client.methods.HttpPost;
//import org.apache.http.impl.client.CloseableHttpClient;
//import org.apache.http.impl.client.HttpClients;
//import org.apache.http.message.BasicNameValuePair;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.web.context.request.RequestContextHolder;
//import org.springframework.web.context.request.ServletRequestAttributes;
//
//import java.io.BufferedReader;
//import java.io.InputStreamReader;
//import java.math.BigDecimal;
//import java.net.URLEncoder;
//import java.nio.charset.StandardCharsets;
//import java.text.SimpleDateFormat;
//import java.time.LocalDateTime;
//import java.util.*;
//
//@Service
//public class PaymentService {
//
//    @Value("${vnp.pay.url}")
//    private String vnpPayUrl;
//
//    @Value("${vnp.tmnCode}")
//    private String vnpTmnCode;
//
//    @Value("${vnp.hashSecret}")
//    private String vnpHashSecret;
//
//    @Value("${vnp.returnUrl}")
//    private String vnpReturnUrl;
//
//    private final BillRepository billRepository;
//    private final BookingRepository bookingRepository;
//    private final OrderRepository orderRepository;
//    private final StatusRepository statusRepository;
//
//    public PaymentService(BillRepository billRepository, BookingRepository bookingRepository, OrderRepository orderRepository, StatusRepository statusRepository) {
//        this.billRepository = billRepository;
//        this.bookingRepository = bookingRepository;
//        this.orderRepository = orderRepository;
//        this.statusRepository = statusRepository;
//    }
//
//    public String createPaymentUrl(PaymentDTO dto) throws Exception {
//        String vnp_Version = "2.1.0";
//        String vnp_Command = "pay";
//        String orderType = "100000";
//        String vnp_TxnRef = dto.getBillID();
//        String vnp_IpAddr = getClientIp();
//        String vnp_Locale = "vn";
//        String vnp_CurrCode = "VND";
//
//        Map<String, String> vnp_Params = new TreeMap<>();
//        vnp_Params.put("vnp_Version", vnp_Version);
//        vnp_Params.put("vnp_Command", vnp_Command);
//        vnp_Params.put("vnp_TmnCode", vnpTmnCode);
//        vnp_Params.put("vnp_Amount", String.valueOf(dto.getAmount() * 100));
//        vnp_Params.put("vnp_CurrCode", vnp_CurrCode);
//        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
//        vnp_Params.put("vnp_OrderInfo", dto.getOrderInfo());
//        vnp_Params.put("vnp_OrderType", orderType);
//        vnp_Params.put("vnp_Locale", vnp_Locale);
//        vnp_Params.put("vnp_ReturnUrl", vnpReturnUrl);
//        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);
//
//        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
//        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
//        String vnp_CreateDate = formatter.format(cld.getTime());
//        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);
//
//        // Tạo query string và secure hash
//        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
//        Collections.sort(fieldNames);
//        StringBuilder hashData = new StringBuilder();
//        StringBuilder query = new StringBuilder();
//
//        for (String fieldName : fieldNames) {
//            String fieldValue = vnp_Params.get(fieldName);
//            if (fieldValue != null && fieldValue.length() > 0) {
//                // Build hashData
//                if (hashData.length() > 0) {
//                    hashData.append('&');
//                }
//                hashData.append(fieldName).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8.toString()));
//
//                // Build query
//                if (query.length() > 0) {
//                    query.append('&');
//                }
//                query.append(fieldName).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8.toString()));
//            }
//        }
//        System.out.println("HashData: " + hashData);
//        String vnp_SecureHash = VnPayUtil.hmacSHA512(vnpHashSecret, hashData.toString());
//        query.append("&vnp_SecureHash=").append(vnp_SecureHash);
//
//        System.out.println("Generated payment URL: " + (vnpPayUrl + "?" + query));
//        return vnpPayUrl + "?" + query;
//    }
//
//    public ResponseData handleCallback(Map<String, String> params) {
//        // Lưu lại secure hash và loại bỏ khỏi params để tính toán chữ ký
//        String vnp_SecureHash = params.get("vnp_SecureHash");
//        String vnp_Version = params.get("vnp_Version");
//        System.out.println("vnp_Version1: " + vnp_Version);
//        System.out.println("params: " + params);
////        params.remove("vnp_SecureHash");
////        params.remove("vnp_SecureHashType");
//        params.remove("vnp_TransactionNo");
//        params.remove("vnp_TransactionStatus");
//
//        // Tạo bản sao của params để tính toán chữ ký
//        Map<String, String> signParams = new TreeMap<>(params);
//        signParams.remove("vnp_SecureHash");
//        signParams.remove("vnp_SecureHashType");
//
//        StringBuilder signData = new StringBuilder();
//        List<String> fieldNames = new ArrayList<>(signParams.keySet());
//        Collections.sort(fieldNames);
//
//        for (String fieldName : fieldNames) {
//            String fieldValue = signParams.get(fieldName);
//            if (fieldValue != null && fieldValue.length() > 0) {
//                if (signData.length() > 0) {
//                    signData.append('&');
//                }
//                System.out.println("Processing field: " + fieldName + "=" + fieldValue);
//                if("vnp_SecureHash".equals(fieldName)) {
//                    signData.append(fieldName).append('=').append(fieldValue); // Không encode vnp_SecureHash
//                } else
//                if ("vnp_OrderInfo".equals(fieldName)) {
//                    signData.append(fieldName).append('=').append(fieldValue); // Không encode vnp_OrderInfo
//                } else {
//                    try {
//                        signData.append(fieldName).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8.toString()));
//                    } catch (Exception e) {
//                        System.err.println("Error encoding parameter: " + fieldName + "=" + fieldValue);
//                        throw new RuntimeException("Error encoding parameter", e);
//                    }
//                }
//            }
//        }
//        if(vnp_Version == null) {
//            vnp_Version = "2.1.0"; // Gán giá trị mặc định nếu vnp_Version không có trong params
//        }
//        signData.append("&vnp_Version=").append(vnp_Version); // Thêm vnp_Version vào cuối chuỗi ký
//        System.out.println("SignData1: " + signData);
////        signData.remove(signData.length() - 1, signData.length()); // Xóa ký tự '&' cuối cùng nếu có
//        String calculatedSignValue = VnPayUtil.hmacSHA512(vnpHashSecret, signData.toString());
//
//        System.out.println("HashSecret used: " + vnpHashSecret);
//        System.out.println("Received params: " + params);
//        System.out.println("SignData2: " + signData);
//        System.out.println("Received vnp_SecureHash: " + vnp_SecureHash);
//        System.out.println("Calculated signValue: " + calculatedSignValue);
//
//        ResponseData response = new ResponseData();
//
//        if (calculatedSignValue.equals(vnp_SecureHash)) {
//            String transactionStatus = params.get("vnp_TransactionStatus");
//            String billID = params.get("vnp_TxnRef");
//
//            // Gọi updateBillAfterPayment với billID dạng Integer
//            ResponseData updateResponse = updateBillAfterPayment(Integer.parseInt(billID), transactionStatus, params);
//            return updateResponse;
//        } else {
//            response.setMessage("Chữ ký không hợp lệ");
//            response.setSuccess(false);
//            response.setStatus(400);
//            return response;
//        }
//    }
//
//    public ResponseData refund(RefundPaymentDTO dto) throws Exception {
//        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
//        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
//        String vnp_CreateDate = formatter.format(cld.getTime());
//        String vnp_TransactionDate = formatter.format(Date.from(dto.getPaymentTime().atZone(java.time.ZoneId.systemDefault()).toInstant()));
//
//        Map<String, String> params = new TreeMap<>();
//        params.put("vnp_Version", "2.1.0");
//        params.put("vnp_Command", "refund");
//        params.put("vnp_TmnCode", vnpTmnCode);
//        params.put("vnp_TxnRef", dto.getBillID());
//        params.put("vnp_Amount", String.valueOf(dto.getAmount() * 100));
//        params.put("vnp_TransactionType", dto.getTransactionType());
//        params.put("vnp_TransactionDate", vnp_TransactionDate);
//        params.put("vnp_CreateDate", vnp_CreateDate);
//        params.put("vnp_IpAddr", getClientIp());
//        params.put("vnp_CreateBy", "system");
//        params.put("vnp_RequestId", UUID.randomUUID().toString());
//        params.put("vnp_OrderInfo", dto.getReason());
//
//        // Tạo chữ ký
//        List<String> fieldNames = new ArrayList<>(params.keySet());
//        Collections.sort(fieldNames);
//        StringBuilder hashData = new StringBuilder();
//
//        for (String fieldName : fieldNames) {
//            String fieldValue = params.get(fieldName);
//            if (fieldValue != null && fieldValue.length() > 0) {
//                if (hashData.length() > 0) {
//                    hashData.append('&');
//                }
//                hashData.append(fieldName).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8.toString()));
//            }
//        }
//
//        String vnp_SecureHash = VnPayUtil.hmacSHA512(vnpHashSecret, hashData.toString());
//        params.put("vnp_SecureHash", vnp_SecureHash);
//
//        // Gửi request refund
//        CloseableHttpClient client = HttpClients.createDefault();
//        HttpPost post = new HttpPost(vnpPayUrl);
//        List<NameValuePair> urlParameters = new ArrayList<>();
//        params.forEach((key, value) -> urlParameters.add(new BasicNameValuePair(key, value)));
//        post.setEntity(new UrlEncodedFormEntity(urlParameters, StandardCharsets.UTF_8));
//
//        try (CloseableHttpResponse httpResponse = client.execute(post);
//             BufferedReader reader = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent(), StandardCharsets.UTF_8))) {
//
//            StringBuilder responseBuilder = new StringBuilder();
//            String inputLine;
//            while ((inputLine = reader.readLine()) != null) {
//                responseBuilder.append(inputLine);
//            }
//
//            Map<String, String> responseMap = parseResponse(responseBuilder.toString());
//            String rspCode = responseMap.get("vnp_ResponseCode");
//
//            ResponseData response = new ResponseData();
//            if ("00".equals(rspCode)) {
//                response.setMessage("Hoàn tiền thành công");
//                response.setSuccess(true);
//                response.setData(responseMap);
//                response.setStatus(200);
//            } else {
//                response.setMessage("Hoàn tiền thất bại: " + rspCode);
//                response.setSuccess(false);
//                response.setStatus(400);
//                response.setData(responseMap);
//            }
//            return response;
//        }
//    }
//
//    @Transactional
//    public ResponseData updateBillAfterPayment(Integer billID, String transactionStatus, Map<String, String> params) {
//        Bill bill = billRepository.findById(billID)
//                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hóa đơn với ID: " + billID));
//
//        ResponseData response = new ResponseData();
//
//        try {
//            if ("00".equals(transactionStatus)) {
//                // Thanh toán thành công
//                BigDecimal paidAmount = new BigDecimal(params.get("vnp_Amount")).divide(new BigDecimal(100)); // Chuyển từ VNĐ sang số tiền thực
//                bill.setInitialPayment(paidAmount);
//                bill.setRemainingAmount(bill.getTotalAmount().subtract(paidAmount));
//                bill.setPaymentMethod("TRANSFER");
//                bill.setPaymentTime(LocalDateTime.now());
//                bill.setTransactionNo(params.get("vnp_TransactionNo"));
//
//                Status successStatus = statusRepository.findByStatusName(
//                                bill.getRemainingAmount().compareTo(BigDecimal.ZERO) == 0 ?
//                                        EStatus.PAID.getName() : EStatus.PARTIALLY_PAID.getName())
//                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy trạng thái"));
//                bill.setStatus(successStatus);
//                billRepository.save(bill);
//
//                // Cập nhật trạng thái booking và order
//                updateRelatedEntities(bill, true);
//
//                response.setMessage("Thanh toán hóa đơn thành công");
//                response.setSuccess(true);
//                response.setStatus(200);
//                response.setData(mapToBillDTO(bill));
//            } else {
//                // Thanh toán thất bại
//                Status failedStatus = statusRepository.findByStatusName(EStatus.FAILED.getName())
//                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy trạng thái FAILED"));
//                bill.setStatus(failedStatus);
//                bill.setInitialPayment(BigDecimal.ZERO);
//                bill.setRemainingAmount(bill.getTotalAmount());
//                bill.setPaymentTime(null);
//                bill.setTransactionNo(null);
//                billRepository.save(bill);
//
//                // Cập nhật trạng thái booking
//                updateRelatedEntities(bill, false);
//
//                response.setMessage("Thanh toán hóa đơn thất bại: " + params.get("vnp_ResponseCode"));
//                response.setSuccess(false);
//                response.setStatus(400);
//                response.setData(mapToBillDTO(bill));
//            }
//        } catch (Exception e) {
//            response.setMessage("Lỗi khi cập nhật hóa đơn: " + e.getMessage());
//            response.setSuccess(false);
//            response.setStatus(500);
//        }
//
//        return response;
//    }
//
//    private void updateRelatedEntities(Bill bill, boolean isSuccess) {
//        if (bill.getBooking() != null) {
//            Booking booking = bill.getBooking();
//            try {
//                if (isSuccess) {
//                    booking.setStatus(statusRepository.findByStatusName(EStatus.CONFIRMED.getName())
//                            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy trạng thái CONFIRMED")));
//                } else {
//                    booking.setStatus(statusRepository.findByStatusName(EStatus.CANCELLED.getName())
//                            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy trạng thái CANCELLED")));
//                }
//                bookingRepository.save(booking);
//            } catch (ResourceNotFoundException e) {
//                System.err.println("Lỗi khi cập nhật booking: " + e.getMessage());
//            }
//        }
//
//        if (bill.getOrder() != null && isSuccess) {
//            Order order = bill.getOrder();
//            try {
//                order.setStatus(statusRepository.findByStatusName(EStatus.PAID.getName())
//                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy trạng thái PAID")));
//                orderRepository.save(order);
//            } catch (ResourceNotFoundException e) {
//                System.err.println("Lỗi khi cập nhật order: " + e.getMessage());
//            }
//        }
//    }
//
//    private BillDTO mapToBillDTO(Bill bill) {
//        BillDTO billDTO = new BillDTO();
//        billDTO.setBillID(bill.getBillID());
//        billDTO.setUserID(bill.getUser() != null ? bill.getUser().getUserID() : null);
//        billDTO.setUserName(bill.getUser() != null ? bill.getUser().getUsername() : null);
//        billDTO.setTableID(bill.getRestaurantTable() != null ? bill.getRestaurantTable().getTableID() : null);
//        billDTO.setTableName(bill.getRestaurantTable() != null ? bill.getRestaurantTable().getTableName() : null);
//        billDTO.setBookingID(bill.getBooking() != null ? bill.getBooking().getBookingID() : null);
//        billDTO.setOrderID(bill.getOrder() != null ? bill.getOrder().getOrderID() : null);
//        billDTO.setBillDate(bill.getBillDate());
//        billDTO.setInitialPayment(bill.getInitialPayment());
//        billDTO.setTotalAmount(bill.getTotalAmount());
//        billDTO.setRemainingAmount(bill.getRemainingAmount());
//        billDTO.setPaymentMethod(bill.getPaymentMethod());
//        billDTO.setPaymentTime(bill.getPaymentTime());
//        billDTO.setStatusID(bill.getStatus() != null ? bill.getStatus().getStatusID() : null);
//        billDTO.setTransactionNo(bill.getTransactionNo());
//        billDTO.setCreatedAt(bill.getCreatedAt());
//        billDTO.setUpdatedAt(bill.getUpdatedAt());
//        return billDTO;
//    }
//
//    private String getClientIp() {
//        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
//        if (attributes != null) {
//            String remoteAddr = attributes.getRequest().getRemoteAddr();
//            // Xử lý trường hợp IPv6 localhost
//            if ("0:0:0:0:0:0:0:1".equals(remoteAddr) || "::1".equals(remoteAddr)) {
//                return "127.0.0.1";
//            }
//            return remoteAddr;
//        }
//        return "127.0.0.1";
//    }
//
//    private Map<String, String> parseResponse(String responseStr) {
//        Map<String, String> map = new HashMap<>();
//        if (responseStr != null && !responseStr.isEmpty()) {
//            for (String param : responseStr.split("&")) {
//                String[] parts = param.split("=", 2);
//                if (parts.length >= 1) {
//                    String key = parts[0];
//                    String value = parts.length > 1 ? parts[1] : "";
//                    map.put(key, value);
//                }
//            }
//        }
//        return map;
//    }
//}