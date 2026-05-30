package com.example.bookingtour.services.MinIO;

import io.minio.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import net.coobird.thumbnailator.Thumbnails;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.UUID;

@Service
public class MinioService {

    @Autowired
    private MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @Value("${minio.endpoint}")
    private String endpoint;

    public String uploadFile(MultipartFile file) throws Exception {

        // Tạo một luồng tạm để nén ảnh
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();

        // Nén ảnh: Resize về chiều rộng tối đa 1920px (giữ tỉ lệ), chất lượng 80%
        Thumbnails.of(file.getInputStream())
                .size(1920, 1080)
                .outputFormat("jpg") // Chuyển hết về JPG cho nhẹ
                .outputQuality(0.8)
                .toOutputStream(outputStream);

        byte[] compressedData = outputStream.toByteArray();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(compressedData);

        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(fileName)
                        .stream(inputStream, compressedData.length, -1)
                        .contentType("image/jpeg")
                        .build()
        );

        return fileName;
    }

    public void deleteFile(String fileUrl) throws Exception {
        String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
        minioClient.removeObject(RemoveObjectArgs.builder()
                .bucket(bucketName)
                .object(fileName)
                .build());
    }
}