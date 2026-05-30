package com.example.bookingtour.dtos.request.tour;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TourImageRequest {
    private Integer tourId;
    private MultipartFile file;
}