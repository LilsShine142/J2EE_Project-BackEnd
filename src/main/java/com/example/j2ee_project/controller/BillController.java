package com.example.j2ee_project.controller;

import com.example.j2ee_project.entity.Bill;
import com.example.j2ee_project.entity.Booking;
import com.example.j2ee_project.entity.Order;
import com.example.j2ee_project.entity.Status;
import com.example.j2ee_project.exception.ResourceNotFoundException;
import com.example.j2ee_project.model.dto.BillDTO;
import com.example.j2ee_project.model.dto.PaymentDTO;
import com.example.j2ee_project.model.dto.RefundPaymentDTO;
import com.example.j2ee_project.model.request.bill.BillForBookingRequestDTO;
import com.example.j2ee_project.model.response.ResponseData;
import com.example.j2ee_project.model.response.ResponseHandler;
import com.example.j2ee_project.repository.BookingRepository;
import com.example.j2ee_project.repository.OrderRepository;
import com.example.j2ee_project.repository.StatusRepository;
import com.example.j2ee_project.service.bill.BillService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Optional;

@RestController
@RequestMapping("/api/bill")
@Tag(name = "Bill Management", description = "APIs for managing restaurant bills and payments")
@RequiredArgsConstructor
public class BillController {

    private final BillService billService;
    private final ResponseHandler responseHandler;
    private final BookingRepository bookingRepository;
    private final OrderRepository orderRepository;
    private final StatusRepository statusRepository;

    /**
     * Lấy thông tin chi tiết của một hóa đơn theo ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ResponseData> getBillById(@PathVariable Integer id) {
        BillDTO bill = billService.getBillById(id);
        Optional<Status> status = bill.getStatusID() != null ? statusRepository.findById(bill.getStatusID()) : null;
        status.ifPresent(s -> {
            bill.setStatusName(s.getStatusName());
            bill.setStatusDescription(s.getDescription());
        });
        return responseHandler.responseSuccess("Lấy thông tin hóa đơn thành công", bill);
    }

    /**
     * Lấy danh sách hóa đơn với phân trang và lọc
     */
    @GetMapping
    public ResponseEntity<ResponseData> getAllBills(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer statusId,
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) Integer tableId) {
        Page<BillDTO> billPage = billService.getAllBills(offset, limit, search, statusId, userId, tableId);
        return responseHandler.responseSuccess("Lấy danh sách hóa đơn thành công", billPage);
    }

    /**
     * Khởi tạo thanh toán cho hóa đơn (tạo URL VNPAY)
     */
    @PostMapping("/{id}/pay")
    public ResponseEntity<ResponseData> initiatePayment(
            @PathVariable Integer id,
            @RequestBody PaymentDTO paymentDTO) throws Exception {
        String paymentUrl = billService.initiatePaymentForBill(id, paymentDTO.getAmount(), paymentDTO.getOrderInfo(), paymentDTO.getPaymentMethod());
        return responseHandler.responseSuccess("Tạo URL thanh toán thành công", paymentUrl);
    }

    /**
     * Yêu cầu hoàn tiền cho hóa đơn
     */
    @PostMapping("/{id}/refund")
    public ResponseEntity<ResponseData> refundBill(
            @PathVariable Integer id,
            @RequestBody RefundPaymentDTO refundDTO) throws Exception {
        ResponseData response = billService.refundBill(id, refundDTO);
        if (response.isSuccess()) {
            return responseHandler.responseSuccess(response.getMessage(), response.getData());
        } else {
            return responseHandler.responseError(response.getMessage(), org.springframework.http.HttpStatus.valueOf(response.getStatus()));
        }
    }

    /**
     * Cập nhật thông tin hóa đơn
     */
    @PutMapping("/{id}")
    public ResponseEntity<ResponseData> updateBill(
            @PathVariable Integer id,
            @RequestBody BillDTO billDetails) {
        BillDTO updatedBill = billService.updateBill(id, billDetails);
        return responseHandler.responseSuccess("Cập nhật hóa đơn thành công", updatedBill);
    }

    /**
     * Tạo hóa đơn cho một Booking và khởi tạo thanh toán
     */
    @PostMapping("/for-booking/{bookingId}")
    public ResponseEntity<ResponseData> createBillForBooking(
            @PathVariable Integer bookingId,
            @Valid @RequestBody BillForBookingRequestDTO requestDTO) throws Exception {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy booking với ID: " + bookingId));

        // Check booking status: only allow if PENDING (3) or CONFIRMED (4), and not REJECTED (12)
        if (booking.getStatus().getStatusID() == 12) {
            throw new IllegalStateException("Không thể tạo hóa đơn cho booking đã bị từ chối");
        } else if (booking.getStatus().getStatusID() != 3 && booking.getStatus().getStatusID() != 4) {
            throw new IllegalStateException("Chỉ có thể tạo hóa đơn cho booking với trạng thái PENDING hoặc CONFIRMED");
        }

        String paymentUrl = billService.createPendingBillAndInitiatePayment(booking, BigDecimal.valueOf(requestDTO.getInitialPayment()),
                requestDTO.getPaymentPercentage(), requestDTO.getVoucherCode(), requestDTO.getOrderInfo());
        return responseHandler.responseSuccess("Tạo hóa đơn tạm thời và khởi tạo thanh toán thành công", paymentUrl);
    }

    /**
     * Tạo hóa đơn cho một Order
     */
    @PostMapping("/for-order/{orderId}")
    public ResponseEntity<ResponseData> createBillForOrder(
            @PathVariable Integer orderId,
            @RequestBody PaymentDTO paymentDTO) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy order với ID: " + orderId));
        BillDTO bill = billService.createBillForOrder(order, paymentDTO.getPaymentMethod());
        return responseHandler.responseSuccess("Tạo hóa đơn cho order thành công", bill);
    }

    /**
     * XUẤT HÓA ĐƠN CUỐI CÙNG TẠI QUÁN (từ Order)
     * → Nhân viên bấm "Xuất hóa đơn" → hoàn tất Order + Booking + giải phóng bàn
     */
    @PostMapping("/checkout-at-restaurant")
    public ResponseEntity<ResponseData> checkoutAtRestaurant(
            @Valid @RequestBody BillForBookingRequestDTO request) {

        try {
            Integer bookingId = request.getBookingId();
            if (bookingId == null) {
                throw new IllegalArgumentException("bookingId là bắt buộc");
            }

            // 1. Tìm Order theo bookingID
            Order order = orderRepository.findByBookingID(bookingId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Order cho booking ID: " + bookingId));

            // 2. Tạo hóa đơn cuối cùng từ Order
            BillDTO finalBill = billService.createFinalBillFromOrder(
                    order,
                    request.getPaymentPercentage(),
                    request.getVoucherCode()
            );

            // 3. Hoàn tất Booking + giải phóng bàn
            billService.completeBookingAfterCheckout(bookingId);

            return responseHandler.responseSuccess(
                    "Xuất hóa đơn thành công! Bàn đã được giải phóng.",
                    finalBill
            );

        } catch (Exception e) {
            return responseHandler.responseError(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}