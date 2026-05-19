package com.example.bookingtour.controllers;

import com.example.bookingtour.IServices.ILeadService;
import com.example.bookingtour.dtos.request.crm.LeadCreateRequest;
import com.example.bookingtour.dtos.request.crm.LeadUpdateRequest;
import com.example.bookingtour.enums.LeadStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/crm/leads")
@RequiredArgsConstructor
public class LeadController {

    private final ILeadService leadService;

    @PostMapping
    public ResponseEntity<?> createLead(@Valid @RequestBody LeadCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(leadService.createLead(request));
    }

    @GetMapping
    public ResponseEntity<?> getLeadsByStaff(@RequestParam Integer staffId) {
        return ResponseEntity.ok(leadService.getLeadsByStaff(staffId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getLeadDetail(@PathVariable Integer id) {
        return ResponseEntity.ok(leadService.getLeadDetail(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateLead(@PathVariable Integer id, @RequestBody LeadUpdateRequest request) {
        return ResponseEntity.ok(leadService.updateLead(id, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Integer id, @RequestParam LeadStatus status) {
        leadService.updateLeadStatus(id, status);
        return ResponseEntity.ok("Cập nhật trạng thái thành công");
    }
}