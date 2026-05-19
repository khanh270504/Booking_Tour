package com.example.bookingtour.services;

import com.example.bookingtour.IServices.ILeadService;
import com.example.bookingtour.dtos.request.crm.LeadCreateRequest;
import com.example.bookingtour.dtos.request.crm.LeadUpdateRequest;
import com.example.bookingtour.dtos.response.crm.LeadResponse;
import com.example.bookingtour.entities.CrmLead;
import com.example.bookingtour.entities.StaffProfile;
import com.example.bookingtour.entities.Tour;
import com.example.bookingtour.enums.LeadStatus;
import com.example.bookingtour.repositories.CrmLeadRepository;
import com.example.bookingtour.repositories.StaffProfileRepository;
import com.example.bookingtour.repositories.TourRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LeadServiceImpl implements ILeadService {

    private final CrmLeadRepository leadRepository;
    private final TourRepository tourRepository;
    private final StaffProfileRepository staffRepository;

    @Override
    @Transactional
    public LeadResponse createLead(LeadCreateRequest request) {
        Tour tour = request.getInterestedTourId() != null
                ? tourRepository.findById(request.getInterestedTourId()).orElse(null) : null;

        CrmLead lead = CrmLead.builder()
                .leadCode("LEAD-" + System.currentTimeMillis())
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .source(request.getSource())
                .status(LeadStatus.NEW)
                .interestedTour(tour)
                .build();

        return LeadResponse.fromLead(leadRepository.save(lead));
    }

    @Override
    @Transactional
    public LeadResponse updateLead(Integer id, LeadUpdateRequest request) {
        CrmLead lead = leadRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Lead"));

        // Cập nhật thông tin (Chỉ cập nhật những field người dùng gửi lên)
        if (request.getFullName() != null) lead.setFullName(request.getFullName());
        if (request.getEmail() != null) lead.setEmail(request.getEmail());
        if (request.getStatus() != null) lead.setStatus(request.getStatus());
        if (request.getPriority() != null) lead.setPriority(request.getPriority());
        if (request.getEstimatedPeople() != null) lead.setEstimatedPeople(request.getEstimatedPeople());
        if (request.getEstimatedBudget() != null) lead.setEstimatedBudget(request.getEstimatedBudget());
        if (request.getNotes() != null) lead.setNotes(request.getNotes());

        // Chuyển giao cho sale khác nếu có
        if (request.getAssignedStaffId() != null) {
            StaffProfile staff = staffRepository.findById(request.getAssignedStaffId()).orElse(null);
            lead.setAssignedStaff(staff);
        }

        lead.setUpdatedAt(Instant.now());
        return LeadResponse.fromLead(leadRepository.save(lead));
    }

    @Override
    @Transactional
    public void updateLeadStatus(Integer id, LeadStatus newStatus) {
        CrmLead lead = leadRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Lead"));
        lead.setStatus(newStatus);
        lead.setUpdatedAt(Instant.now());
        leadRepository.save(lead);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeadResponse> getLeadsByStaff(Integer staffId) {
        return leadRepository.findByAssignedStaff_IdOrderByCreatedAtDesc(staffId).stream()
                .map(LeadResponse::fromLead)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public LeadResponse getLeadDetail(Integer id) {
        CrmLead lead = leadRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Lead"));
        return LeadResponse.fromLead(lead);
    }
}