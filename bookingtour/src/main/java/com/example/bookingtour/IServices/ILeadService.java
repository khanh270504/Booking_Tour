package com.example.bookingtour.IServices;

import com.example.bookingtour.dtos.request.crm.LeadConvertRequest;
import com.example.bookingtour.dtos.request.crm.LeadCreateRequest;
import com.example.bookingtour.dtos.request.crm.LeadUpdateRequest;
import com.example.bookingtour.dtos.response.crm.LeadResponse;
import com.example.bookingtour.enums.LeadStatus;

import java.util.List;

public interface ILeadService {
    LeadResponse createLead(LeadCreateRequest request);

    void updateLeadStatus(Integer leadId, LeadStatus newStatus);

    List<LeadResponse> getLeads(Integer staffId);

    LeadResponse getLeadDetail(Integer id);

    LeadResponse updateLead(Integer leadId, LeadUpdateRequest leadUpdateRequest);
    LeadResponse changeLeadStatus(Integer leadId, LeadStatus newStatus, Integer userInternalId);
}
