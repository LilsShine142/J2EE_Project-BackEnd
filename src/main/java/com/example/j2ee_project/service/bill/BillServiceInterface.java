package com.example.j2ee_project.service.bill;

import com.example.j2ee_project.entity.Booking;
import com.example.j2ee_project.entity.Order;
import com.example.j2ee_project.model.dto.BillDTO;
import com.example.j2ee_project.model.dto.RefundPaymentDTO;
import com.example.j2ee_project.model.response.ResponseData;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.Map;

public interface BillServiceInterface {

    /**
     * Tạo hóa đơn cho một đặt bàn (Booking).
     * @param booking Đặt bàn liên quan
     * @param mealTotal Tổng tiền bữa ăn
     * @param paymentPercentage Phần trăm thanh toán (30 hoặc 100)
     * @param voucherCode Mã voucher (tùy chọn, để áp dụng giảm giá)
     * @return Hóa đơn đã được tạo dưới dạng BillDTO
     */
    String createPendingBillAndInitiatePayment(Booking booking, BigDecimal mealTotal, double paymentPercentage, String voucherCode, String orderInfo) throws Exception;

    /**
     * Tạo hóa đơn cho một đơn hàng (Order).
     * @param order Đơn hàng liên quan
     * @param paymentMethod Phương thức thanh toán (CASH hoặc TRANSFER)
     * @return Hóa đơn đã được tạo dưới dạng BillDTO
     */
    BillDTO createBillForOrder(Order order, String paymentMethod);

    /**
     * Khởi tạo thanh toán cho hóa đơn, trả về URL thanh toán VNPAY.
     * @param billID ID của hóa đơn
     * @param amount Số tiền thanh toán
     * @param orderInfo Thông tin mô tả giao dịch
     * @param paymentMethod Phương thức thanh toán
     * @return URL thanh toán
     * @throws Exception Nếu có lỗi trong quá trình tạo URL
     */
    String initiatePaymentForBill(Integer billID, long amount, String orderInfo, String paymentMethod) throws Exception;

    /**
     * Hủy hóa đơn cho một đặt bàn.
     *
     * @param billID ID của hóa đơn
     * @return
     * @throws Exception Nếu có lỗi trong quá trình hủy hóa đơn
     */
    ResponseData cancelBillForBooking(Integer billID) throws Exception;

    /**
     * Xử lý hoàn tiền cho hóa đơn.
     * @param billID ID của hóa đơn
     * @param dto Thông tin yêu cầu hoàn tiền
     * @return ResponseData chứa kết quả hoàn tiền
     * @throws Exception Nếu có lỗi trong quá trình hoàn tiền
     */
    ResponseData refundBill(Integer billID, RefundPaymentDTO dto) throws Exception;

    /**
     * Lấy thông tin chi tiết của một hóa đơn theo ID.
     * @param billID ID của hóa đơn
     * @return Hóa đơn tương ứng dưới dạng BillDTO
     */
    BillDTO getBillById(Integer billID);

    /**
     * Lấy danh sách hóa đơn với phân trang và lọc.
     * @param offset Vị trí bắt đầu
     * @param limit Số lượng bản ghi mỗi trang
     * @param search Từ khóa tìm kiếm
     * @param statusId ID trạng thái hóa đơn
     * @param userId ID người dùng
     * @param tableId ID bàn
     * @return Danh sách hóa đơn dạng phân trang dưới dạng BillDTO
     */
    Page<BillDTO> getAllBills(int offset, int limit, String search, Integer statusId, Integer userId, Integer tableId);

    /**
     * Cập nhật thông tin hóa đơn.
     * @param billID ID của hóa đơn
     * @param billDetails Thông tin hóa đơn cần cập nhật
     * @return Hóa đơn đã được cập nhật dưới dạng BillDTO
     */
    BillDTO updateBill(Integer billID, BillDTO billDetails);
}