package com.meta.travel.controller;

import com.meta.travel.common.Result;
import com.meta.travel.dto.request.AlbumRequest;
import com.meta.travel.dto.response.AlbumVO;
import com.meta.travel.security.UserContext;
import com.meta.travel.service.AlbumService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 旅行相册接口（对应 lvxingxiangce.vue）
 */
@RestController
@RequestMapping("/api/albums")
@RequiredArgsConstructor
public class AlbumController {

    private final AlbumService albumService;

    @GetMapping
    public Result<List<AlbumVO>> list() {
        return Result.success(albumService.listByUser(UserContext.currentUserId()));
    }

    @GetMapping("/{id}")
    public Result<AlbumVO> detail(@PathVariable Long id) {
        return Result.success(albumService.getById(UserContext.currentUserId(), id));
    }

    @PostMapping
    public Result<AlbumVO> create(@Valid @RequestBody AlbumRequest request) {
        return Result.success("相册已保存", albumService.save(UserContext.currentUserId(), null, request));
    }

    @PutMapping("/{id}")
    public Result<AlbumVO> update(@PathVariable Long id, @Valid @RequestBody AlbumRequest request) {
        return Result.success("相册已更新", albumService.save(UserContext.currentUserId(), id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        albumService.delete(UserContext.currentUserId(), id);
        return Result.success("相册已删除", null);
    }
}
