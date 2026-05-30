package com.example.bookingtour.services;

import com.example.bookingtour.IServices.IProviderService;
import com.example.bookingtour.dtos.request.operation.ProviderRequest;
import com.example.bookingtour.dtos.response.operation.ProviderResponse;
import com.example.bookingtour.entities.Provider;
import com.example.bookingtour.enums.ProviderStatus;
import com.example.bookingtour.enums.ServiceType;
import com.example.bookingtour.exceptions.AppException;
import com.example.bookingtour.exceptions.ErrorCode;
import com.example.bookingtour.repositories.ProviderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProviderServiceImpl implements IProviderService {

    private final ProviderRepository providerRepository;

    @Override
    @Transactional
    public ProviderResponse createProvider(ProviderRequest request) {
        // Có thể thêm logic check trùng Email hoặc SĐT ở đây nếu cần

        Provider provider = Provider.builder()
                .providerCode(generateProviderCode())
                .name(request.getName())
                .serviceType(ServiceType.valueOf(request.getServiceType().toUpperCase()))
                .contactPerson(request.getContactPerson())
                .phone(request.getPhone())
                .email(request.getEmail())
                .address(request.getAddress())
                .status(request.getStatus())
                .build();

        Provider savedProvider = providerRepository.save(provider);
        return ProviderResponse.fromEntity(savedProvider, 0L, BigDecimal.ZERO);
    }

    @Override
    @Transactional
    public ProviderResponse updateProvider(Integer id, ProviderRequest request) {
        Provider provider = providerRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PROVIDER_NOT_FOUND)); // Sếp nhớ thêm enum lỗi này

        provider.setName(request.getName());
        provider.setServiceType(ServiceType.valueOf(request.getServiceType().toUpperCase()));
        provider.setContactPerson(request.getContactPerson());
        provider.setPhone(request.getPhone());
        provider.setEmail(request.getEmail());
        provider.setAddress(request.getAddress());
        provider.setStatus(request.getStatus());

        providerRepository.save(provider);
        // Tạm thời truyền 0 và ZERO, nếu sếp viết câu query thống kê rồi thì lấy nạp vào đây
        return ProviderResponse.fromEntity(provider, 0L, BigDecimal.ZERO);
    }

    @Override
    public ProviderResponse getProviderById(Integer id) {
        Provider provider = providerRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PROVIDER_NOT_FOUND));
        return ProviderResponse.fromEntity(provider, 0L, BigDecimal.ZERO);
    }

    @Override
    public List<ProviderResponse> getAllProviders() {
        // Lưu ý: Sau này sếp có thể tối ưu bằng câu JPQL gộp bảng để ra được count và sum
        return providerRepository.findAll().stream()
                .map(p -> ProviderResponse.fromEntity(p, 0L, BigDecimal.ZERO))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteProvider(Integer id) {
        Provider provider = providerRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PROVIDER_NOT_FOUND));
        // Nên là Soft Delete (đổi status = INACTIVE) để không vỡ data cũ
        provider.setStatus(ProviderStatus.INACTIVE);
        providerRepository.save(provider);
    }

    @Override
    @Transactional
    public ProviderResponse changeStatus(Integer id, ProviderStatus status) {
        Provider provider = providerRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PROVIDER_NOT_FOUND));
        provider.setStatus(status);
        providerRepository.save(provider);
        return ProviderResponse.fromEntity(provider, 0L, BigDecimal.ZERO);
    }

    private String generateProviderCode() {
        // Sinh mã ngẫu nhiên hoặc theo sequence. VD: SUP-12345
        return "SUP-" + (System.currentTimeMillis() % 100000);
    }
}
