package com.example.bookingtour.services;

import com.example.bookingtour.dtos.request.tour.TourImageRequest;
import com.example.bookingtour.entities.Tour;
import com.example.bookingtour.entities.TourImage;
import com.example.bookingtour.exceptions.AppException;
import com.example.bookingtour.exceptions.ErrorCode;
import com.example.bookingtour.repositories.TourImageRepository;
import com.example.bookingtour.repositories.TourRepository;
import com.example.bookingtour.services.MinIO.MinioService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TourImageService {

    MinioService minioService;
    TourImageRepository tourImageRepository;
    TourRepository tourRepository;

    @Transactional
    public String uploadAndSave(TourImageRequest request) throws Exception {
        String fileUrl = minioService.uploadFile(request.getFile());

        Tour tour = tourRepository.findById(request.getTourId())
                .orElseThrow(() -> new AppException(ErrorCode.TOUR_NOT_FOUND));

        TourImage tourImage = TourImage.builder()
                .tour(tour)
                .imageUrl(fileUrl)
                .build();

        tourImageRepository.save(tourImage);

        return fileUrl;
    }
    public String uploadOnlyToMinIO(MultipartFile file) throws Exception {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File không được để trống!");
        }
        String fileName = minioService.uploadFile(file);
        return fileName;
    }
    public List<Map<String, Object>> getAllRawImages() {
        return tourImageRepository.findAll().stream()
                .map(ti -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", ti.getId());
                    map.put("imageUrl", ti.getImageUrl());
                    map.put("tourId", ti.getTour() != null ? ti.getTour().getId() : null);
                    return map;
                })
                .toList();
    }
    @Transactional
    public void deleteTourImage(Integer imageId) throws Exception {
        TourImage tourImage = tourImageRepository.findById(imageId)
                .orElseThrow(() -> new AppException(ErrorCode.TOUR_NOT_FOUND)); // Hoặc lỗi IMAGE_NOT_FOUND của sếp

        if (tourImage.getImageUrl() != null && !tourImage.getImageUrl().isEmpty()) {
            try {
                minioService.deleteFile(tourImage.getImageUrl());
            } catch (Exception e) {
                System.out.println("Cảnh báo: Không tìm thấy file trên MinIO để xóa, tiến hành xóa DB luôn.");
            }
        }

        tourImageRepository.delete(tourImage);
    }
}
