package com.example.j2ee_project.controller;

import com.example.j2ee_project.model.dto.PaymentDTO;
import com.example.j2ee_project.model.dto.RefundPaymentDTO;
import com.example.j2ee_project.model.response.ResponseData;
import com.example.j2ee_project.model.response.ResponseHandler;
import com.example.j2ee_project.service.bill.BillService;
import com.example.j2ee_project.service.payment.PaymentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@Tag(name = "Payment Management", description = "APIs for managing payment transactions")
public class PaymentController {

    private final BillService billService;
    private final ResponseHandler responseHandler;
    private final PaymentService paymentService;
    public PaymentController(BillService billService, ResponseHandler responseHandler, PaymentService paymentService) {
        this.billService = billService;
        this.responseHandler = responseHandler;
        this.paymentService = paymentService;
    }

    @PostMapping("/create")
    public ResponseEntity<ResponseData> createPayment(@RequestBody PaymentDTO dto) throws Exception {
        String paymentUrl = billService.initiatePaymentForBill(Integer.parseInt(dto.getBillID()), dto.getAmount(), dto.getOrderInfo(), dto.getPaymentMethod());
        return responseHandler.responseSuccess("Tạo URL thanh toán thành công", paymentUrl);
    }

    @GetMapping("/callback")
    public ResponseEntity<ResponseData> handlePaymentReturn(@RequestParam Map<String, String> params) throws Exception {
        ResponseData response = paymentService.handleCallback(params);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @GetMapping("/ipn")
    public ResponseEntity<ResponseData> handlePaymentIPN(@RequestParam Map<String, String> params) throws Exception {
        ResponseData response = paymentService.handleCallback(params);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PostMapping("/refund")
    public ResponseEntity<ResponseData> refund(@RequestBody RefundPaymentDTO dto) throws Exception {
        ResponseData response = billService.refundBill(Integer.parseInt(dto.getBillID()), dto);
        if (response.isSuccess()) {
            return responseHandler.responseSuccess(response.getMessage(), response.getData());
        } else {
            return responseHandler.responseError(response.getMessage(), org.springframework.http.HttpStatus.valueOf(response.getStatus()));
        }
    }
}