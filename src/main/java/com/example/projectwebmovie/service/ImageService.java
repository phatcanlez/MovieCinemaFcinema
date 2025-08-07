package com.example.projectwebmovie.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ImageService {

    private static final String UPLOAD_DIR = "src/main/resources/static/images/avatar_account/";

    public String saveAvatarImage(MultipartFile image) throws Exception {
        if (image.isEmpty()) {
            return UPLOAD_DIR;
        }
        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            String fileName = System.currentTimeMillis() + "_"
                    + image.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(image.getInputStream(), filePath);
            return "/images/avatar_account/" + fileName;
        } catch (Exception e) {
            throw new Exception("Lỗi khi lưu ảnh: " + e.getMessage(), e);
        }
    }
}
