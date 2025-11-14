package com.elevate.consultingplatform.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
public class ZohoConfig {

    @Value("${zoho.client.id}")
    private String clientId;

    @Value("${zoho.client.secret}")
    private String clientSecret;

    @Value("${zoho.refresh.token}")
    private String refreshToken;

    @Value("${zoho.crm.datacenter}")
    private String dataCenter;

    private Integer httpConnectTimeoutMs = 10000;
    private Integer httpReadTimeoutMs = 20000;


    public String getAccountsUrl() {
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


    public String getApiUrl() {
        String dc = (dataCenter == null ? "US" : dataCenter).trim().toUpperCase();
        return switch (dc) {
            case "IN" -> "https://www.zohoapis.in";
            case "EU" -> "https://www.zohoapis.eu";
            case "AU" -> "https://www.zohoapis.com.au";
            case "JP" -> "https://www.zohoapis.jp";
            case "CN" -> "https://www.zohoapis.com.cn";
            default -> "https://www.zohoapis.com";
        };
    }
}
