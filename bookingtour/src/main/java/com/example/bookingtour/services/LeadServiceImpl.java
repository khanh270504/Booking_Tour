package com.example.bookingtour.services;

import com.example.bookingtour.IServices.ILeadService;
import com.example.bookingtour.dtos.request.crm.LeadCreateRequest;
import com.example.bookingtour.dtos.request.crm.LeadUpdateRequest;
import com.example.bookingtour.dtos.response.crm.LeadResponse;
import com.example.bookingtour.entities.CrmLead;
import com.example.bookingtour.entities.StaffProfile;
import com.example.bookingtour.entities.Tour;
import com.example.bookingtour.enums.LeadSource;
import com.example.bookingtour.enums.LeadStatus;
import com.example.bookingtour.repositories.CrmLeadRepository;
import com.example.bookingtour.repositories.StaffProfileRepository;
import com.example.bookingtour.repositories.TourRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeadServiceImpl implements ILeadService {

    private final CrmLeadRepository leadRepository;
    private final TourRepository tourRepository;
    private final StaffProfileRepository staffRepository;

    // Chỉ giữ lại TaskService để tạo nhắc việc
    private final TaskServiceImpl taskService;

    @Override
    @Transactional
    public LeadResponse createLead(LeadCreateRequest request) {
        Tour tour = request.getInterestedTourId() != null
                ? tourRepository.findById(request.getInterestedTourId()).orElse(null) : null;
        StaffProfile staff = request.getAssignedStaffId() != null
                ? staffRepository.findById(request.getAssignedStaffId()).orElse(null) : null;

        CrmLead lead = CrmLead.builder()
                .leadCode("LEAD-" + System.currentTimeMillis())
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .source(request.getSource() != null ? LeadSource.valueOf(request.getSource().toUpperCase()) : null)
                .status(LeadStatus.NEW)
                .interestedTour(tour)
                .assignedStaff(staff)
                .notes(request.getNotes())
                .build();

        return LeadResponse.fromLead(leadRepository.save(lead));
    }

    @Override
    @Transactional
    public LeadResponse updateLead(Integer id, LeadUpdateRequest request) {
        CrmLead lead = leadRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Lead"));

        if (request.getFullName() != null) lead.setFullName(request.getFullName());
        if (request.getPhone() != null) lead.setPhone(request.getPhone());
        if (request.getEmail() != null) lead.setEmail(request.getEmail());
        if (request.getSource() != null) lead.setSource(request.getSource());
        if (request.getPriority() != null) lead.setPriority(request.getPriority());
        if (request.getEstimatedPeople() != null) lead.setEstimatedPeople(request.getEstimatedPeople());
        if (request.getEstimatedBudget() != null) lead.setEstimatedBudget(request.getEstimatedBudget());
        if (request.getExpectedTravelDate() != null) lead.setExpectedTravelDate(request.getExpectedTravelDate());
        if (request.getNotes() != null) lead.setNotes(request.getNotes());

        if (request.getInterestedTourId() != null) {
            Tour tour = tourRepository.findById(request.getInterestedTourId()).orElse(null);
            lead.setInterestedTour(tour);
        }

        if (request.getAssignedStaffId() != null) {
            StaffProfile staff = staffRepository.findById(request.getAssignedStaffId()).orElse(null);
            lead.setAssignedStaff(staff);
        }

        if (request.getStatus() == LeadStatus.LOST) {
            lead.setLostReason(request.getLostReason());
        }

        if (request.getStatus() != null) lead.setStatus(request.getStatus());
        lead.setUpdatedAt(Instant.now());

        return LeadResponse.fromLead(leadRepository.save(lead));
    }

    @Override
    @Transactional
    public void updateLeadStatus(Integer id, LeadStatus newStatus) {
        CrmLead lead = leadRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Lead"));

        if (newStatus == LeadStatus.WON && lead.getStatus() != LeadStatus.WON) {
            taskService.createBookingReminderTask(lead);
            log.info("🎯 Lead {} đã chuyển sang WON, đã tạo Task nhắc Sale chốt đơn.", lead.getFullName());
        }

        lead.setStatus(newStatus);
        lead.setUpdatedAt(Instant.now());
        leadRepository.save(lead);
    }

    @Override
    public List<LeadResponse> getLeads(Integer requestedStaffId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        Jwt jwt = (Jwt) auth.getPrincipal();
        Integer currentUserId = Math.toIntExact(jwt.getClaim("userId"));

        if (isAdmin) {
            if (requestedStaffId != null) {
                return leadRepository.findByAssignedStaff_IdOrderByCreatedAtDesc(requestedStaffId).stream()
                        .map(LeadResponse::fromLead)
                        .toList();
            } else {
                return leadRepository.findAllByOrderByCreatedAtDesc().stream()
                        .map(LeadResponse::fromLead)
                        .toList();
            }
        } else {
            return leadRepository.findByAssignedStaff_IdOrderByCreatedAtDesc(currentUserId).stream()
                    .map(LeadResponse::fromLead)
                    .toList();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public LeadResponse getLeadDetail(Integer id) {
        CrmLead lead = leadRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Lead"));
        return LeadResponse.fromLead(lead);
    }
}