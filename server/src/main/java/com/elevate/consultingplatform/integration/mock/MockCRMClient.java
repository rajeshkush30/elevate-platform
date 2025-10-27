package com.elevate.consultingplatform.integration.mock;

import com.elevate.consultingplatform.integration.CRMClient;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile({"mock", "default"})
public class MockCRMClient implements CRMClient {
    @Override
    public CreateLeadResponse createLead(CreateLeadRequest req) {
        return new CreateLeadResponse("ZOH_LEAD_123");
    }

    @Override
    public ConvertLeadResponse convertLead(ConvertLeadRequest req) {
        return new ConvertLeadResponse("ZOH_CONT_555");
    }

    @Override
    public void addNote(AddNoteRequest req) {
        // no-op in mock
    }
}
