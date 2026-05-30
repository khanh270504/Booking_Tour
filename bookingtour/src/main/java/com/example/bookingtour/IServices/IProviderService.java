package com.example.bookingtour.IServices;

import com.example.bookingtour.dtos.response.operation.ProviderResponse;
import com.example.bookingtour.dtos.request.operation.ProviderRequest;
import com.example.bookingtour.enums.ProviderStatus;

import java.util.List;

public interface IProviderService {
    ProviderResponse createProvider(ProviderRequest request);
    ProviderResponse updateProvider(Integer id, ProviderRequest request);
    ProviderResponse getProviderById(Integer id);
    List<ProviderResponse> getAllProviders();
    void deleteProvider(Integer id);
    ProviderResponse changeStatus(Integer id, ProviderStatus status);
}
