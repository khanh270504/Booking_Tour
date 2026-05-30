package com.example.bookingtour.controllers.MinIO;

import com.example.bookingtour.dtos.response.ApiResponse;
import com.example.bookingtour.services.MinIO.MinioService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FileController {

    MinioService minioService;

    @PostMapping("/upload")
    public ApiResponse<String> upload(@RequestParam("file") MultipartFile file) throws Exception {
        String fileUrl = minioService.uploadFile(file);

        return ApiResponse.<String>builder()
                .result(fileUrl)
                .message("Upload ảnh thành công!")
                .build();
    }

    @DeleteMapping("/delete")
    public ApiResponse<Void> delete(@RequestParam("fileUrl") String fileUrl) throws Exception {
        minioService.deleteFile(fileUrl);

        return ApiResponse.<Void>builder()
                .message("Xóa ảnh thành công!")
                .build();
    }
}