package com.example.bookingtour.controllers;

import com.example.bookingtour.IServices.ILeadService;
import com.example.bookingtour.dtos.request.crm.LeadConvertRequest;
import com.example.bookingtour.dtos.request.crm.LeadCreateRequest;
import com.example.bookingtour.dtos.request.crm.LeadUpdateRequest;
import com.example.bookingtour.dtos.response.ApiResponse;
import com.example.bookingtour.enums.LeadStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/crm/leads")
@RequiredArgsConstructor
public class LeadController {

    private final ILeadService leadService;

    @PostMapping
    public ApiResponse<?> createLead(@Valid @RequestBody LeadCreateRequest request) {
        return ApiResponse.builder()
                .code(200)
                .result(leadService.createLead(request))
                .build();
    }

    @GetMapping
    public ApiResponse<?> getLeads(@RequestParam(required = false) Integer staffId) {
        return ApiResponse.builder()
                .code(200)
                .result(leadService.getLeads(staffId))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<?> getLeadDetail(@PathVariable Integer id) {
        return ApiResponse.builder()
                .code(200)
                .result(leadService.getLeadDetail(id))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<?> updateLead(@PathVariable Integer id, @RequestBody LeadUpdateRequest request) {
        return ApiResponse.builder()
                .code(200)
                .result(leadService.updateLead(id, request))
                .build();
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<?> updateStatus(
            @PathVariable("id") Integer id,
            @RequestParam("status") LeadStatus status,
            Authentication authentication) {

        Integer userInternalId = null;
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            Long uId = jwt.getClaim("userId");
            if (uId != null) {
                userInternalId = uId.intValue();
            }
        }

        return ApiResponse.builder()
                .code(200)
                .message("Cập nhật trạng thái thành công")
                .result(leadService.changeLeadStatus(id, status, userInternalId)) // Chạy hàm đẻ Booking + Payment ngầm
                .build();
    }
}