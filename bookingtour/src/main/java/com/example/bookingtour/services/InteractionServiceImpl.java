package com.example.bookingtour.services;

import com.example.bookingtour.IServices.IInteractionService;
import com.example.bookingtour.dtos.request.crm.InteractionCreateRequest;
import com.example.bookingtour.dtos.response.crm.InteractionResponse;
import com.example.bookingtour.entities.CrmInteraction;
import com.example.bookingtour.entities.CrmLead;
import com.example.bookingtour.entities.StaffProfile;
import com.example.bookingtour.repositories.CrmInteractionRepository;
import com.example.bookingtour.repositories.CrmLeadRepository;
import com.example.bookingtour.repositories.StaffProfileRepository;
import com.example.bookingtour.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InteractionServiceImpl implements IInteractionService {

    private final CrmInteractionRepository interactionRepository;
    private final CrmLeadRepository leadRepository;
    private final StaffProfileRepository staffRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public InteractionResponse logInteraction(InteractionCreateRequest request) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        Jwt jwt = (Jwt) authentication.getPrincipal();
        Integer userId = ((Number) jwt.getClaim("userId")).intValue();
        StaffProfile staff = staffRepository.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));

        CrmLead lead = leadRepository.findById(request.getLeadId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));

        CrmInteraction interaction = CrmInteraction.builder()
                .lead(lead)
                .staff(staff)
                .interactionType(request.getInteractionType())

                .result(request.getResult())

                .note(request.getNote())
                .nextActionDate(request.getNextActionDate())
                .createdAt(Instant.now())
                .build();

        CrmInteraction savedInteraction = interactionRepository.save(interaction);

        lead.setLastContactAt(Instant.now());


        leadRepository.save(lead);

        return InteractionResponse.fromInteractionResponse(savedInteraction);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InteractionResponse> getHistoryByLead(Integer leadId) {
        return interactionRepository.findByLead_IdOrderByCreatedAtDesc(leadId).stream()
                .map(InteractionResponse::fromInteractionResponse)
                .toList();
    }
}