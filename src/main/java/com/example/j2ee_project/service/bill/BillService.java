package com.example.j2ee_project.service.bill;

import com.example.j2ee_project.entity.*;
import com.example.j2ee_project.exception.ResourceNotFoundException;
import com.example.j2ee_project.model.dto.BillDTO;
import com.example.j2ee_project.model.dto.PaymentDTO;
import com.example.j2ee_project.model.dto.RefundPaymentDTO;
import com.example.j2ee_project.model.response.ResponseData;
import com.example.j2ee_project.repository.BillRepository;
import com.example.j2ee_project.repository.BookingRepository;
import com.example.j2ee_project.repository.OrderRepository;
import com.example.j2ee_project.repository.StatusRepository;
import com.example.j2ee_project.repository.VoucherRepository;
import com.example.j2ee_project.service.payment.PaymentService;
import com.example.j2ee_project.utils._enum.EStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BillService implements BillServiceInterface {

    private final BillRepository billRepository;
    private final BookingRepository bookingRepository;
    private final OrderRepository orderRepository;
    private final StatusRepository statusRepository;
    private final PaymentService paymentService;
    private final VoucherRepository voucherRepository;

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

    @Transactional
    public String createPendingBillAndInitiatePayment(Booking booking, BigDecimal mealTotal, double paymentPercentage, String voucherCode, String orderInfo) throws Exception {
        if (paymentPercentage != 30.0 && paymentPercentage != 100.0) {
            throw new IllegalArgumentException("Phần trăm thanh toán phải là 30% hoặc 100%");
        }

        Status pendingStatus = statusRepository.findByStatusName(EStatus.PENDING.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy trạng thái PENDING"));

        BigDecimal totalAmount = mealTotal;
        if (voucherCode != null && !voucherCode.isEmpty()) {
            Voucher voucher = voucherRepository.findByVoucherCode(voucherCode)
                    .orElseThrow(() -> new ResourceNotFoundException("Mã voucher không hợp lệ: " + voucherCode));
            totalAmount = totalAmount
                    .subtract(totalAmount.multiply(BigDecimal.valueOf(voucher.getDiscountPercentage()))
                            .divide(BigDecimal.valueOf(100)))
                    .max(BigDecimal.ZERO);
        }

        BigDecimal initialPayment = totalAmount.multiply(BigDecimal.valueOf(paymentPercentage / 100.0))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal remainingAmount = totalAmount.subtract(initialPayment)
                .setScale(2, RoundingMode.HALF_UP);

        Bill existingBill = billRepository.findByBookingId(booking.getBookingID());
        Bill bill;
        if (existingBill != null && existingBill.getStatus().getStatusName().equals(EStatus.FAILED.getName())) {
            bill = existingBill;
            bill.setInitialPayment(initialPayment);
            bill.setTotalAmount(totalAmount);
            bill.setRemainingAmount(initialPayment);
            bill.setPaymentMethod("TRANSFER");
            bill.setStatus(pendingStatus);
            bill.setUpdatedAt(LocalDateTime.now());
        } else {
            bill = new Bill();
            bill.setUser(booking.getUser());
            bill.setRestaurantTable(booking.getRestaurantTable());
            bill.setBooking(booking);
            bill.setBillDate(LocalDate.now());
            bill.setInitialPayment(initialPayment);
            bill.setTotalAmount(totalAmount);
            bill.setRemainingAmount(initialPayment);
            bill.setPaymentMethod("TRANSFER");
            bill.setStatus(pendingStatus);
            bill.setCreatedAt(LocalDateTime.now());
            bill.setUpdatedAt(LocalDateTime.now());
        }

        Bill savedBill = billRepository.save(bill);
        return initiatePaymentForBill(savedBill.getBillID(), initialPayment.longValue(), orderInfo, "TRANSFER");
    }

    @Transactional
    public BillDTO createBillForOrder(Order order, String paymentMethod) {
        if (!paymentMethod.equals("CASH") && !paymentMethod.equals("TRANSFER")) {
            throw new IllegalArgumentException("Phương thức thanh toán phải là CASH hoặc TRANSFER");
        }

        Status status = statusRepository.findByStatusName(EStatus.PAID.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy trạng thái PAID"));

        BigDecimal mealTotal = order.getOrderDetails().stream()
                .map(detail -> detail.getSubTotal())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Bill bill = new Bill();
        bill.setUser(order.getUser());
        bill.setRestaurantTable(order.getRestaurantTable());
        bill.setOrder(order);
        bill.setBillDate(LocalDate.now());
        bill.setMealTotal(mealTotal);
        bill.setTotalAmount(mealTotal);
        bill.setInitialPayment(mealTotal);
        bill.setRemainingAmount(BigDecimal.ZERO);
        bill.setPaymentMethod(paymentMethod);
        bill.setStatus(status);
        bill.setCreatedAt(LocalDateTime.now());
        bill.setUpdatedAt(LocalDateTime.now());

        if (order.getBookingID() != null) {
            Bill existingBill = billRepository.findByBookingId(order.getBookingID());
            if (existingBill != null) {
                existingBill.setOrder(order);
                existingBill.setMealTotal(mealTotal);
                existingBill.setTotalAmount(existingBill.getInitialPayment().add(mealTotal));
                existingBill.setInitialPayment(existingBill.getInitialPayment().add(mealTotal));
                existingBill.setRemainingAmount(BigDecimal.ZERO);
                existingBill.setPaymentMethod(paymentMethod);
                existingBill.setStatus(status);
                return mapToBillDTO(billRepository.save(existingBill));
            }
        }

        return mapToBillDTO(billRepository.save(bill));
    }

    @Transactional
    public String initiatePaymentForBill(Integer billID, long amount, String orderInfo, String paymentMethod) throws Exception {
        Bill bill = billRepository.findById(billID)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hóa đơn với ID: " + billID));

        if (bill.getBooking() != null && !paymentMethod.equals("TRANSFER")) {
            throw new IllegalArgumentException("Phương thức thanh toán cho booking phải là TRANSFER");
        }
        if (bill.getOrder() != null && !paymentMethod.equals("CASH") && !paymentMethod.equals("TRANSFER")) {
            throw new IllegalArgumentException("Phương thức thanh toán cho order phải là CASH hoặc TRANSFER");
        }

        BigDecimal paymentAmount = BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP);
        if (bill.getRemainingAmount().compareTo(paymentAmount) < 0) {
            throw new IllegalStateException("Số tiền thanh toán vượt quá số tiền còn lại");
        }

        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setBillID(String.valueOf(billID));
        paymentDTO.setAmount(amount);
        paymentDTO.setOrderInfo(orderInfo);
        paymentDTO.setPaymentMethod(paymentMethod);

        return paymentService.createPaymentUrl(paymentDTO);
    }

    @Transactional
    public ResponseData cancelBillForBooking(Integer bookingId) throws Exception {
        Bill bill = billRepository.findByBookingId(bookingId);
        if (bill == null) {
            return new ResponseData() {{
                setSuccess(false);
                setMessage("Không tìm thấy hóa đơn liên quan đến booking ID: %d".formatted(bookingId));
                setStatus(404);
                setData(null);
            }};
        }

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy booking với ID: " + bookingId));

        ResponseData response = new ResponseData();
        LocalDateTime now = LocalDateTime.now();
        boolean canRefund = bill.getPaymentTime() != null &&
                ChronoUnit.HOURS.between(bill.getPaymentTime(), now) <= 2 &&
                (bill.getStatus().getStatusName().equals(EStatus.PAID.getName()) ||
                        bill.getStatus().getStatusName().equals(EStatus.PARTIALLY_PAID.getName())) &&
                bill.getInitialPayment().compareTo(BigDecimal.ZERO) > 0;

        if (canRefund) {
            RefundPaymentDTO refundDTO = new RefundPaymentDTO();
            refundDTO.setBillID(String.valueOf(bill.getBillID()));
            refundDTO.setAmount(bill.getInitialPayment().longValue());
            refundDTO.setReason("Hoàn tiền hủy booking #" + bookingId);
            refundDTO.setTransactionType(bill.getRemainingAmount().equals(BigDecimal.ZERO) ? "02" : "03");
            refundDTO.setPaymentTime(bill.getPaymentTime());
            refundDTO.setTransactionNo(bill.getTransactionNo());

            ResponseData refundResponse = paymentService.refund(refundDTO);
            System.out.println("Refund response: " + refundResponse);
            if (refundResponse.isSuccess()) {
                bill.setStatus(statusRepository.findByStatusName(EStatus.REFUNDED.getName())
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy trạng thái REFUNDED")));
                bill.setUpdatedAt(now);
                // Lưu transactionNo từ refund response
                if (refundResponse.getData() instanceof Map) {
                    Map<String, String> refundData = (Map<String, String>) refundResponse.getData();
                    bill.setTransactionNo(refundData.get("vnp_TransactionNo"));
                }
                billRepository.save(bill);

                if (bill.getBooking() != null) {
                    Booking bookingEntity = bill.getBooking();
                    bookingEntity.setStatus(statusRepository.findByStatusName(EStatus.CANCELLED.getName())
                            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy trạng thái CANCELLED")));
                    bookingRepository.save(bookingEntity);
                }

                response.setMessage("Hủy bill và hoàn tiền thành công");
                response.setSuccess(true);
                response.setData(mapToBillDTO(bill));
            } else {
                response.setMessage("Hủy bill thất bại do lỗi hoàn tiền: " + refundResponse.getMessage());
                response.setSuccess(false);
                response.setStatus(400);
                response.setData(mapToBillDTO(bill));
            }
        } else {
            bill.setStatus(statusRepository.findByStatusName(EStatus.CANCELLED.getName())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy trạng thái CANCELLED")));
            bill.setUpdatedAt(now);
            billRepository.save(bill);

            if (bill.getBooking() != null) {
                Booking bookingEntity = bill.getBooking();
                bookingEntity.setStatus(statusRepository.findByStatusName(EStatus.CANCELLED.getName())
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy trạng thái CANCELLED")));
                bookingRepository.save(bookingEntity);
            }

            response.setMessage("Hủy bill thành công (không hoàn tiền do quá 2 giờ hoặc trạng thái không hợp lệ)");
            response.setSuccess(true);
            response.setData(mapToBillDTO(bill));
        }

        return response;
    }

    @Transactional
    public ResponseData refundBill(Integer billID, RefundPaymentDTO dto) throws Exception {
        Bill bill = billRepository.findById(billID)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hóa đơn với ID: " + billID));

        if (!bill.getStatus().getStatusName().equals(EStatus.PAID.getName()) &&
                !bill.getStatus().getStatusName().equals(EStatus.PARTIALLY_PAID.getName())) {
            throw new IllegalStateException("Hóa đơn không ở trạng thái có thể hoàn tiền");
        }

        if (!dto.getBillID().equals(String.valueOf(billID))) {
            throw new IllegalArgumentException("ID hóa đơn trong DTO không khớp với billID");
        }

        if (dto.getAmount() > bill.getInitialPayment().longValue()) {
            throw new IllegalStateException("Số tiền hoàn vượt quá số tiền đã thanh toán");
        }

        if (!dto.getTransactionType().equals("02") && !dto.getTransactionType().equals("03")) {
            throw new IllegalArgumentException("Loại giao dịch hoàn tiền phải là '02' (toàn bộ) hoặc '03' (một phần)");
        }

        if (dto.getPaymentTime() != null && !dto.getPaymentTime().equals(bill.getPaymentTime())) {
            throw new IllegalStateException("Thời gian thanh toán không khớp với hóa đơn");
        }

        ResponseData response = paymentService.refund(dto);

        if (response.isSuccess()) {
            bill.setStatus(statusRepository.findByStatusName(EStatus.REFUNDED.getName())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy trạng thái REFUNDED")));
            bill.setUpdatedAt(LocalDateTime.now());
            // Lưu transactionNo từ refund response
            if (response.getData() instanceof Map) {
                Map<String, String> refundData = (Map<String, String>) response.getData();
                bill.setTransactionNo(refundData.get("vnp_TransactionNo"));
            }
            billRepository.save(bill);

            if (bill.getBooking() != null) {
                Booking booking = bill.getBooking();
                booking.setStatus(statusRepository.findByStatusName(EStatus.CANCELLED.getName())
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy trạng thái CANCELLED")));
                bookingRepository.save(booking);
            }
            if (bill.getOrder() != null) {
                Order order = bill.getOrder();
                order.setStatus(statusRepository.findByStatusName(EStatus.CANCELLED.getName())
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy trạng thái CANCELLED")));
                orderRepository.save(order);
            }

            response.setData(mapToBillDTO(bill));
        }

        return response;
    }

    @Transactional(readOnly = true)
    public BillDTO getBillById(Integer billID) {
        Bill bill = billRepository.findById(billID)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hóa đơn với ID: " + billID));
        return mapToBillDTO(bill);
    }

    @Transactional(readOnly = true)
    public Page<BillDTO> getAllBills(int offset, int limit, String search, Integer statusId, Integer userId, Integer tableId) {
        if (offset < 0) offset = 0;
        if (limit <= 0) limit = 10;
        if (limit > 100) limit = 100;

        if (search == null) search = "";

        Pageable pageable = PageRequest.of(offset / limit, limit);
        return billRepository.findByFilters(search, statusId, userId, tableId, pageable)
                .map(this::mapToBillDTO);
    }

    @Transactional
    public BillDTO updateBill(Integer billID, BillDTO billDetails) {
        Bill bill = billRepository.findById(billID)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hóa đơn với ID: " + billID));

        if (billDetails.getPaymentMethod() != null) {
            if (bill.getBooking() != null && !billDetails.getPaymentMethod().equals("TRANSFER")) {
                throw new IllegalArgumentException("Phương thức thanh toán cho booking phải là TRANSFER");
            }
            if (bill.getOrder() != null && !billDetails.getPaymentMethod().equals("CASH") && !billDetails.getPaymentMethod().equals("TRANSFER")) {
                throw new IllegalArgumentException("Phương thức thanh toán cho order phải là CASH hoặc TRANSFER");
            }
            bill.setPaymentMethod(billDetails.getPaymentMethod());
        }
        if (billDetails.getStatusID() != null) {
            Status status = statusRepository.findById(billDetails.getStatusID())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy trạng thái với ID: " + billDetails.getStatusID()));
            bill.setStatus(status);
        }
        bill.setUpdatedAt(LocalDateTime.now());

        return mapToBillDTO(billRepository.save(bill));
    }
}