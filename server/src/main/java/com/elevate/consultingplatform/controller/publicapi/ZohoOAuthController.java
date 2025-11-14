package com.elevate.consultingplatform.controller.publicapi;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ZohoOAuthController {

    /**
     * This endpoint handles the OAuth callback from Zoho
     * The "code" parameter will be used to get access & refresh tokens
     */
    @GetMapping("/api/zoho/callback")
    public String zohoCallback(@RequestParam("code") String code) {
        // Print the authorization code to console
        System.out.println("Authorization code received: " + code);

        // Here you can call Zoho token endpoint to exchange code for access & refresh token

        return "Authorization code received: " + code;
    }
}
