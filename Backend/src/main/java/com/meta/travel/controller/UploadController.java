package com.meta.travel.controller;

import com.meta.travel.common.Result;
import com.meta.travel.service.UploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 文件上传接口（头像、相册照片）
 */
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class UploadController {

    private final UploadService uploadService;

    @PostMapping("/upload")
    public Result<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
        String url = uploadService.upload(file);
        return Result.success("上传成功", Map.of("url", url));
    }
}
