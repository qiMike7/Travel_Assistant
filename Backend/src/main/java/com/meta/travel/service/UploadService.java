package com.meta.travel.service;

import com.meta.travel.common.BusinessException;
import com.meta.travel.common.ResultCode;
import com.meta.travel.config.UploadProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * 图片上传服务：保存到本地目录并返回可访问 URL。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UploadService {

    private static final Set<String> ALLOWED_EXT = Set.of("jpg", "jpeg", "png", "gif", "webp", "bmp");

    private final UploadProperties uploadProperties;

    public String upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "上传文件不能为空");
        }
        String original = StringUtils.cleanPath(file.getOriginalFilename() == null ? "" : file.getOriginalFilename());
        String ext = StringUtils.getFilenameExtension(original);
        ext = ext == null ? "jpg" : ext.toLowerCase(Locale.ROOT);
        if (!ALLOWED_EXT.contains(ext)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "仅支持图片文件: " + ALLOWED_EXT);
        }

        String datePath = LocalDate.now().toString();
        String filename = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        try {
            Path dir = Paths.get(uploadProperties.getDir(), datePath).toAbsolutePath().normalize();
            Files.createDirectories(dir);
            Path target = dir.resolve(filename);
            file.transferTo(target.toFile());
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "文件上传失败");
        }
        return uploadProperties.getUrlPrefix() + "/" + datePath + "/" + filename;
    }
}
