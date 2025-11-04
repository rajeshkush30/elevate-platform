package com.elevate.consultingplatform.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "zoho.crm")
public class ZohoCrmProperties {
    /** e.g. https://www.zohoapis.in */
    private String baseUrl;
    /** e.g. IN, US, EU to derive accounts domain */
    private String dataCenter;

    private String clientId;
    private String clientSecret;
    private String refreshToken;

    private Integer httpConnectTimeoutMs = 10000;
    private Integer httpReadTimeoutMs = 20000;

    public String getAccountsBaseUrl() {
        // derive accounts domain by DC; defaults to .com
        String dc = (dataCenter == null ? "US" : dataCenter).trim().toUpperCase();
        return switch (dc) {
            case "IN" -> "https://accounts.zoho.in";
            case "EU" -> "https://accounts.zoho.eu";
            case "AU" -> "https://accounts.zoho.com.au";
            case "JP" -> "https://accounts.zoho.jp";
            case "CN" -> "https://accounts.zoho.com.cn";
            default -> "https://accounts.zoho.com";
        };
    }
}
