package com.elevate.consultingplatform.service.zoho.impl;

import com.elevate.consultingplatform.entity.zoholeads.Lead;
import com.elevate.consultingplatform.entity.zoholeads.LeadStatus;
import com.elevate.consultingplatform.repository.zoholeads.LeadRepository;
import com.elevate.consultingplatform.service.EmailService;
import com.elevate.consultingplatform.service.activation.ActivationLinkService;
import com.elevate.consultingplatform.service.zoho.ZohoWebhookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ZohoWebhookServiceImpl implements ZohoWebhookService {
    private final ActivationLinkService activationLinkService;
    private final LeadRepository leadRepository;

    // Optional: Secure your webhook
    private static final String WEBHOOK_TOKEN = "Xx9YpQ2sT7aR5";

    @Override
    public boolean verifyToken(String token) {
        if (token == null) {
            log.warn("Missing Zoho webhook token");
            return false;
        }
        boolean valid = WEBHOOK_TOKEN.equals(token);
        if (!valid) {
            log.error(" Invalid Zoho webhook token: {}", token);
        }
        return valid;
    }

    @Override
    public Map<String, Object> processLeadStatusUpdate(Map<String, Object> payload) {
        try {
            log.info("Received Zoho webhook payload: {}", payload);

            String zohoLeadId = (String) payload.get("id");
            if (zohoLeadId == null) {
                log.error("Missing 'id' in webhook payload");
                return Map.of("success", false, "message", "Missing Zoho Lead ID");

            }

            String statusStr = (String) payload.get("status");
            String company = (String) payload.get("company");
            LeadStatus newStatus;
            try {
                newStatus = LeadStatus.valueOf(statusStr.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                log.error("Invalid status '{}' received from Zoho", statusStr);
                return Map.of("success", false, "message", e.getMessage());

            }

            Optional<Lead> leadOpt = leadRepository.findByZohoLeadId(zohoLeadId);


                log.info("Lead not found for Zoho ID: {}, creating new record", zohoLeadId);

                Lead lead = leadOpt.orElseGet(() -> Lead.builder()
                        .zohoLeadId(zohoLeadId)
                        .firstName((String) payload.get("firstname"))
                        .lastName((String) payload.get("lastname"))
                        .email((String) payload.get("email"))
                        .phone((String) payload.get("phone"))
                        .company((String) payload.get("company"))
                        .ainotseScore(payload.get("ainotseScore") != null ? (Integer) payload.get("ainotseScore") : null)
                        .status(newStatus)
                        .source((String) payload.get("source"))
                        .ainotseSummary((String) payload.get("ainotseSummary"))
                        .build());

                lead.setStatus(newStatus);
            leadRepository.save(lead);
            log.info("Lead {} saved/updated with status {}", lead.getEmail(), newStatus);
            String activationUrl = null;
            String token = null;

            if (newStatus == LeadStatus.APPROVED) {
             token = UUID.randomUUID().toString();lead.setInviteToken(token);

                    activationUrl = activationLinkService.buildActivationLink(lead.getEmail(), zohoLeadId);



                log.info(" Approved lead - token generated and activation URL ready");
            } else if (newStatus == LeadStatus.REJECTED) {
                log.info("Lead rejected - no activation URL/token created");
            }


            log.info("Lead {} saved/updated with status {}", lead.getEmail(), newStatus);

            return Map.of(
                    "success", true,
                    "status", newStatus.name(),
                    "activationLink", activationUrl != null ? activationUrl : "",
                    "token", token != null ? token : ""
            );


        } catch (Exception e) {
            log.error("Error while processing Zoho webhook: {}", e.getMessage(), e);
            return Map.of("success", false, "message", e.getMessage());

        }
    }
}


