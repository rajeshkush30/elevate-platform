package com.elevate.consultingplatform.integration;

public interface CRMClient {
    record CreateLeadRequest(String name, String email, String company, String source, Double preScore, String stageHint) {}
    record CreateLeadResponse(String leadId) {}

    record ConvertLeadRequest(String leadId) {}
    record ConvertLeadResponse(String contactId) {}

    record AddNoteRequest(String contactId, String title, String content) {}

    CreateLeadResponse createLead(CreateLeadRequest req);
    ConvertLeadResponse convertLead(ConvertLeadRequest req);
    void addNote(AddNoteRequest req);
}
