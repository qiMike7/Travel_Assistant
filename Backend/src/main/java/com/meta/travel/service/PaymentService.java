package com.meta.travel.service;

import com.meta.travel.common.BusinessException;
import com.meta.travel.common.ResultCode;
import com.meta.travel.config.PayProperties;
import com.meta.travel.dto.request.PaymentRequest;
import com.meta.travel.dto.response.WxPayParamsVO;
import com.meta.travel.entity.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

/**
 * Payment service: validates order ownership and builds WeChat Pay JSAPI params.
 * If merchant credentials are not configured, returns deterministic mock params.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String NONCE_CHARS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    private final OrderService orderService;
    private final PayProperties payProperties;
    private final ResourceLoader resourceLoader;

    public WxPayParamsVO createWxPayParams(Long userId, PaymentRequest request) {
        // verify the order exists and belongs to the current user; amount trusts DB, not client
        Order order = orderService.getById(userId, request.getOrderId());

        String timeStamp = String.valueOf(System.currentTimeMillis() / 1000);
        String nonceStr = generateNonceStr(32);

        PrivateKey privateKey = loadPrivateKey();
        if (privateKey == null || !hasText(payProperties.getAppId()) || !hasText(payProperties.getMchId())) {
            log.warn("WeChat Pay merchant params not configured; returning mock pay params for order {}",
                    order.getOrderNo());
            return mockParams(timeStamp, nonceStr, order.getOrderNo());
        }

        // In production the prepay_id must come from the WeChat v3 JSAPI unified-order call.
        // Here we derive a stable prepay_id from the order number and sign per WeChat spec.
        String packageStr = "prepay_id=wx" + order.getOrderNo();
        String message = payProperties.getAppId() + "\n" + timeStamp + "\n" + nonceStr + "\n" + packageStr + "\n";
        String paySign = sign(message, privateKey);
        String signType = hasText(payProperties.getSignType()) ? payProperties.getSignType() : "RSA";

        return new WxPayParamsVO(payProperties.getAppId(), timeStamp, nonceStr, packageStr, signType, paySign);
    }

    private WxPayParamsVO mockParams(String timeStamp, String nonceStr, String orderNo) {
        String appId = hasText(payProperties.getAppId()) ? payProperties.getAppId() : "wxmock000000000000";
        return new WxPayParamsVO(appId, timeStamp, nonceStr, "prepay_id=mock_" + orderNo, "RSA", "MOCK_SIGN_" + nonceStr);
    }

    private PrivateKey loadPrivateKey() {
        String location = payProperties.getPrivateKeyPath();
        if (!hasText(location)) {
            return null;
        }
        try {
            Resource resource = resourceLoader.getResource(location);
            if (!resource.exists()) {
                log.warn("WeChat Pay private key not found at {}", location);
                return null;
            }
            String pem = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8)
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");
            byte[] der = Base64.getDecoder().decode(pem);
            return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(der));
        } catch (Exception e) {
            log.warn("Failed to load WeChat Pay private key: {}", e.getMessage());
            return null;
        }
    }

    private String sign(String message, PrivateKey privateKey) {
        try {
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
            signature.update(message.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(signature.sign());
        } catch (Exception e) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "支付签名失败: " + e.getMessage());
        }
    }

    private String generateNonceStr(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(NONCE_CHARS.charAt(RANDOM.nextInt(NONCE_CHARS.length())));
        }
        return sb.toString();
    }

    private boolean hasText(String s) {
        return s != null && !s.trim().isEmpty();
    }
}
