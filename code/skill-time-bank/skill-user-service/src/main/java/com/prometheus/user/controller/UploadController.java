package com.prometheus.user.controller;

import com.prometheus.common.Result;
import com.prometheus.common.annotation.RequireAuth;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/upload")
public class UploadController {

    private static final long MAX_SIZE = 5 * 1024 * 1024;

    @PostMapping("/avatar")
    @RequireAuth
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file,
                                       HttpServletRequest request) {
        return uploadImage(file, "avatars", request);
    }

    @PostMapping("/image")
    @RequireAuth
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file,
                                      HttpServletRequest request) {
        return uploadImage(file, "images", request);
    }

    private Result<String> uploadImage(MultipartFile file, String subDir,
                                        HttpServletRequest request) {
        if (file.isEmpty()) {
            return Result.fail("请选择图片");
        }
        if (file.getSize() > MAX_SIZE) {
            return Result.fail("图片不能超过5MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return Result.fail("仅支持图片格式");
        }

        try {
            String ext = contentType.substring(contentType.lastIndexOf("/") + 1);
            if (ext.equals("jpeg")) ext = "jpg";
            String filename = UUID.randomUUID() + "." + ext;

            Path uploadDir = Paths.get("uploads", subDir);
            Files.createDirectories(uploadDir);
            file.transferTo(uploadDir.resolve(filename));

            String url = "/uploads/" + subDir + "/" + filename;
            log.info("图片上传成功: userId={}, url={}, dir={}",
                    request.getAttribute("userId"), url, subDir);
            return Result.success(url);
        } catch (IOException e) {
            log.error("图片上传失败", e);
            return Result.fail("上传失败");
        }
    }
}
