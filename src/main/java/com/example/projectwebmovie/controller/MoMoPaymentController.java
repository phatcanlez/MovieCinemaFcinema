package com.example.projectwebmovie.controller;

import com.example.projectwebmovie.dto.momo.MoMoCallbackRequest;
import com.example.projectwebmovie.service.MoMoPaymentService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/api/public/payment")
@RequiredArgsConstructor
@Slf4j
public class MoMoPaymentController {

    private final MoMoPaymentService moMoPaymentService;

    @PostMapping("/initiate-momo")
    public String initiateMoMoPayment() {
        try {
            String payUrl = moMoPaymentService.createPaymentRequest(123L, 10000, "Test Order");
            return "redirect:" + payUrl;
        } catch (Exception e) {
            log.error("Error initiating MoMo payment: {}", e.getMessage());
            return "redirect:/payment-failed";
        }
    }

    @PostMapping("/momo-notify")
    @ResponseBody
    public ResponseEntity<String> handleMoMoNotify(@RequestBody MoMoCallbackRequest callbackRequest) {
        log.info("Received MoMo notify callback for orderId: {}", callbackRequest.getOrderId());
        boolean success = moMoPaymentService.handleMoMoCallback(callbackRequest);
        if (success) {
            return ResponseEntity.ok("OK");
        } else {
            return ResponseEntity.badRequest().body("FAILED");
        }
    }

    @GetMapping("/momo-return")
    public void handleMoMoReturn(HttpServletRequest request, HttpServletResponse response) {
        MoMoCallbackRequest callbackRequest = extractCallbackRequest(request);
        log.info("MoMo return callback - OrderId: {}, ResultCode: {}, Message: {}",
                callbackRequest.getOrderId(), callbackRequest.getResultCode(), callbackRequest.getMessage());
        moMoPaymentService.handleMoMoCallbackWithRedirect(callbackRequest, response);
    }

    private MoMoCallbackRequest extractCallbackRequest(HttpServletRequest request) {
        return MoMoCallbackRequest.builder()
                .partnerCode(request.getParameter("partnerCode"))
                .orderId(request.getParameter("orderId"))
                .requestId(request.getParameter("requestId"))
                .amount(request.getParameter("amount"))
                .orderInfo(request.getParameter("orderInfo"))
                .orderType(request.getParameter("orderType"))
                .transId(request.getParameter("transId"))
                .resultCode(request.getParameter("resultCode"))
                .message(request.getParameter("message"))
                .payType(request.getParameter("payType"))
                .responseTime(request.getParameter("responseTime"))
                .extraData(request.getParameter("extraData") != null ? request.getParameter("extraData") : "")
                .signature(request.getParameter("signature"))
                .build();
    }

    @GetMapping("/status/{orderId}")
    public String checkPaymentStatus(@PathVariable String orderId) {
        log.info("Checking payment status for orderId: {}", orderId);
        // Thêm logic kiểm tra trạng thái nếu cần
        return "payment/status"; // Trả về template Thymeleaf để hiển thị trạng thái
    }

    @GetMapping("/test")
    public String testPayment() {
        log.info("Testing MoMo payment endpoint");
        // Thêm logic kiểm tra nếu cần
        return "payment/initiate"; // Trả về template Thymeleaf để hiển thị kết quả kiểm tra
    }
}