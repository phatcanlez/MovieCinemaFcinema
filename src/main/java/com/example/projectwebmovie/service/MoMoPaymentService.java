package com.example.projectwebmovie.service;

import com.example.projectwebmovie.config.MoMoConfig;
import com.example.projectwebmovie.dto.momo.MoMoCallbackRequest;
import com.example.projectwebmovie.dto.momo.MoMoPaymentRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MoMoPaymentService {

    private final MoMoConfig moMoConfig;
    private final ObjectMapper objectMapper;

    private static final String SUCCESS_URL = "http://localhost:8081/payment-success";
    private static final String FAILED_URL = "http://localhost:8081/payment-failed";
    private static final int SCORE_PER_AMOUNT = 10000; // 10,000 VNĐ = 1 điểm

    public String createPaymentRequest(Long invoiceId, double amount, String orderInfo) throws Exception {
        String orderId = UUID.randomUUID().toString();
        String requestId = UUID.randomUUID().toString();
        String amountStr = String.valueOf((long) amount); // MoMo expects amount in VND as integer string

        // Tạo raw data cho chữ ký theo đúng thứ tự a-z
        String rawData = String.format(
                "accessKey=%s&amount=%s&extraData=%s&ipnUrl=%s&orderId=%s&orderInfo=%s&partnerCode=%s&redirectUrl=%s&requestId=%s&requestType=%s",
                moMoConfig.getAccessKey(),
                amountStr,
                "",
                moMoConfig.getIpnUrl(), // Use ipnUrl from MoMoConfig
                orderId,
                orderInfo,
                moMoConfig.getPartnerCode(),
                moMoConfig.getRedirectUrl(),
                requestId,
                "captureWallet"
        );
        log.debug("Raw data for signature: {}", rawData);
        String signature = generateSignature(rawData, moMoConfig.getSecretKey());
        log.debug("Generated signature: {}", signature);

        // Tạo MoMoPaymentRequest
        MoMoPaymentRequest request = MoMoPaymentRequest.builder()
                .partnerCode(moMoConfig.getPartnerCode())
                .requestId(requestId)
                .amount(amountStr)
                .orderId(orderId)
                .orderInfo(orderInfo)
                .redirectUrl(moMoConfig.getRedirectUrl())
                .ipnUrl(moMoConfig.getIpnUrl()) // Use ipnUrl from MoMoConfig
                .requestType("captureWallet")
                .extraData("")
                .lang("vi")
                .signature(signature)
                .build();

        // Gửi yêu cầu đến MoMo
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<MoMoPaymentRequest> entity = new HttpEntity<>(request, headers);
        log.debug("Sending MoMo request: {}", objectMapper.writeValueAsString(request));

        try {
            String response = restTemplate.postForObject(moMoConfig.getEndpoint(), entity, String.class);
            log.debug("MoMo response: {}", response);
            // Parse response để lấy payUrl
            return objectMapper.readTree(response).get("payUrl").asText();
        } catch (Exception e) {
            log.error("Error sending request to MoMo: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initiate MoMo payment", e);
        }
    }

    @Transactional
    public void handleMoMoCallbackWithRedirect(MoMoCallbackRequest callbackRequest, HttpServletResponse response) {
        try {
            // Verify signature
            String rawData = String.format(
                    "accessKey=%s&amount=%s&extraData=%s&message=%s&orderId=%s&orderInfo=%s&orderType=%s&partnerCode=%s&payType=%s&requestId=%s&responseTime=%s&resultCode=%s&transId=%s",
                    moMoConfig.getAccessKey(),
                    callbackRequest.getAmount(),
                    callbackRequest.getExtraData(),
                    callbackRequest.getMessage(),
                    callbackRequest.getOrderId(),
                    callbackRequest.getOrderInfo(),
                    callbackRequest.getOrderType(),
                    callbackRequest.getPartnerCode(),
                    callbackRequest.getPayType(),
                    callbackRequest.getRequestId(),
                    callbackRequest.getResponseTime(),
                    callbackRequest.getResultCode(),
                    callbackRequest.getTransId()
            );

            String expectedSignature = generateSignature(rawData, moMoConfig.getSecretKey());

            if (!expectedSignature.equals(callbackRequest.getSignature())) {
                log.error("Invalid MoMo signature for order: {}", callbackRequest.getOrderId());
                redirectToFailure(null, response);
                return;
            }

        } catch (Exception e) {
            log.error("Error handling MoMo callback: {}", e.getMessage(), e);
            redirectToFailure(null, response);
        }
    }

    @Transactional
    public boolean handleMoMoCallback(MoMoCallbackRequest callbackRequest) {
        try {
            // Verify signature
            String rawData = String.format(
                    "accessKey=%s&amount=%s&extraData=%s&message=%s&orderId=%s&orderInfo=%s&orderType=%s&partnerCode=%s&payType=%s&requestId=%s&responseTime=%s&resultCode=%s&transId=%s",
                    moMoConfig.getAccessKey(),
                    callbackRequest.getAmount(),
                    callbackRequest.getExtraData(),
                    callbackRequest.getMessage(),
                    callbackRequest.getOrderId(),
                    callbackRequest.getOrderInfo(),
                    callbackRequest.getOrderType(),
                    callbackRequest.getPartnerCode(),
                    callbackRequest.getPayType(),
                    callbackRequest.getRequestId(),
                    callbackRequest.getResponseTime(),
                    callbackRequest.getResultCode(),
                    callbackRequest.getTransId()
            );

            String expectedSignature = generateSignature(rawData, moMoConfig.getSecretKey());

            if (!expectedSignature.equals(callbackRequest.getSignature())) {
                log.error("Invalid MoMo signature for order: {}", callbackRequest.getOrderId());
                return false;
            }

            return true;
        } catch (Exception e) {
            log.error("Error handling MoMo callback: {}", e.getMessage());
            return false;
        }
    }

    private void redirectToFailure(String redirectUrlParam, HttpServletResponse response) {
        try {
            String redirectUrl = redirectUrlParam != null && !redirectUrlParam.isEmpty()
                    ? redirectUrlParam : FAILED_URL;
            redirectUrl += (redirectUrl.contains("?") ? "&" : "?") + "status=failed";
            response.sendRedirect(redirectUrl);
            log.info("Redirecting to failure URL: {}", redirectUrl);
        } catch (IOException e) {
            log.error("Redirect failed: {}", e.getMessage());
            throw new RuntimeException("Redirect failed", e);
        }
    }

    private String generateSignature(String rawData, String secretKey) {
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256_HMAC.init(secret_key);

            byte[] hash = sha256_HMAC.doFinal(rawData.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder();
            for (byte b : hash) {
                result.append(String.format("%02x", b));
            }
            return result.toString();
        } catch (Exception e) {
            log.error("Error generating MoMo signature: {}", e.getMessage());
            throw new RuntimeException("Error generating signature", e);
        }
    }
}
