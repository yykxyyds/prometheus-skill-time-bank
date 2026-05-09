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

            Path uploadDir = Paths.get("uploads", "avatars");
            Files.createDirectories(uploadDir);
            file.transferTo(uploadDir.resolve(filename));

            String url = "/uploads/avatars/" + filename;
            log.info("头像上传成功: userId={}, url={}", request.getAttribute("userId"), url);
            return Result.success(url);
        } catch (IOException e) {
            log.error("头像上传失败", e);
            return Result.fail("上传失败");
        }
    }
}
