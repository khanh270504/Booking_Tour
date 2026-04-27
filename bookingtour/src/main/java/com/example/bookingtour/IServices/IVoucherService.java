package com.example.bookingtour.IServices;

import com.example.bookingtour.dtos.request.sales.VoucherApplyRequest;
import com.example.bookingtour.dtos.request.sales.VoucherCreateRequest;
import com.example.bookingtour.dtos.response.sales.VoucherApplyResponse;
import com.example.bookingtour.dtos.response.sales.VoucherResponse;

import java.util.List;

public interface IVoucherService {
    //ADMIN
    VoucherResponse createVoucher(VoucherCreateRequest request);

    List<VoucherResponse> getAllVouchers();

    VoucherResponse getVoucherById(Integer id);

    VoucherResponse updateVoucher(Integer id, VoucherCreateRequest request);

    void deleteVoucher(Integer id);

    //CUS
    VoucherApplyResponse applyVoucher(VoucherApplyRequest request);
    void redeemVoucher(String code);

    VoucherResponse getVoucherByCode(String code);
}