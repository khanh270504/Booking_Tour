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
}
