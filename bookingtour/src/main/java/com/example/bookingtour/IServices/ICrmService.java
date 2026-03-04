package com.example.bookingtour.IServices;

import com.example.bookingtour.dtos.request.crm.InteractionLogRequest;
import com.example.bookingtour.dtos.request.crm.LeadCreateRequest;
import com.example.bookingtour.dtos.response.crm.InteractionResponse;
import com.example.bookingtour.dtos.response.crm.LeadResponse;

import java.util.List;

public interface ICrmService {

    LeadResponse createLead(LeadCreateRequest request);


    InteractionResponse logInteraction(InteractionLogRequest request);


    List<LeadResponse> getLeadsByStaff(String staffId);
}