package com.meta.travel.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Parameters required to invoke the WeChat Pay cashier on the client.
 * The "package" field is a Java keyword, so it is exposed to JSON via @JsonProperty.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WxPayParamsVO {

    private String appId;

    private String timeStamp;

    private String nonceStr;

    /** WeChat JSAPI package string, e.g. prepay_id=wx... */
    @JsonProperty("package")
    private String packageValue;

    private String signType;

    private String paySign;
}
