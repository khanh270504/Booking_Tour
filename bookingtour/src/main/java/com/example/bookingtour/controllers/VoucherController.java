package com.example.bookingtour.controllers;

import com.example.bookingtour.dtos.request.sales.VoucherApplyRequest;
import com.example.bookingtour.dtos.request.sales.VoucherCreateRequest;
import com.example.bookingtour.dtos.response.sales.VoucherApplyResponse;
import com.example.bookingtour.dtos.response.sales.VoucherResponse;
import com.example.bookingtour.IServices.IVoucherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/vouchers")
@RequiredArgsConstructor
public class VoucherController {

    private final IVoucherService voucherService;

    @PostMapping("/admin/create")
    public ResponseEntity<VoucherResponse> createVoucher(@Valid @RequestBody VoucherCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(voucherService.createVoucher(request));
    }

    @GetMapping("/admin")
    public ResponseEntity<List<VoucherResponse>> getAllVouchers() {
        return ResponseEntity.ok(voucherService.getAllVouchers());
    }

    @GetMapping("/admin/{id}")
    public ResponseEntity<VoucherResponse> getVoucherById(@PathVariable Integer id) {
        return ResponseEntity.ok(voucherService.getVoucherById(id));
    }

    @PutMapping("/admin/{id}")
    public ResponseEntity<VoucherResponse> updateVoucher(
            @PathVariable Integer id,
            @Valid @RequestBody VoucherCreateRequest request) {
        return ResponseEntity.ok(voucherService.updateVoucher(id, request));
    }

    @DeleteMapping("/admin/{id}")
    public ResponseEntity<String> deleteVoucher(@PathVariable Integer id) {
        voucherService.deleteVoucher(id);
        return ResponseEntity.ok("Xóa Voucher thành công (Lưu ý: Tính năng xóa cứng đang bị chặn)");
    }

    @GetMapping("/public/code/{code}")
    public ResponseEntity<VoucherResponse> getVoucherByCode(@PathVariable String code) {
        return ResponseEntity.ok(voucherService.getVoucherByCode(code));
    }

    @PostMapping("/public/apply")
    public ResponseEntity<VoucherApplyResponse> applyVoucher(@Valid @RequestBody VoucherApplyRequest request) {
        return ResponseEntity.ok(voucherService.applyVoucher(request));
    }
}