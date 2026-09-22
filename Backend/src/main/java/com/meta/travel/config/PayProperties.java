package com.meta.travel.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * WeChat Pay config (prefix travel.pay.*).
 * When appId/mchId/privateKeyPath are not configured, /api/payments/wxpay
 * returns mock params so the front-end order flow can be exercised end-to-end.
 */
@Data
@ConfigurationProperties(prefix = "travel.pay")
public class PayProperties {

    /** mini-program / official-account appId */
    private String appId;

    /** merchant id */
    private String mchId;

    /** APIv3 key */
    private String apiV3Key;

    /** merchant certificate serial number */
    private String merchantSerialNumber;

    /** merchant API private key location, supports classpath: or file: prefix */
    private String privateKeyPath;

    /** payment result notify url */
    private String notifyUrl;

    /** sign type, default RSA */
    private String signType = "RSA";
}
