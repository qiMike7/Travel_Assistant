package com.meta.travel.service;

import com.meta.travel.common.BusinessException;
import com.meta.travel.common.ResultCode;
import com.meta.travel.dto.request.AlbumRequest;
import com.meta.travel.dto.response.AlbumVO;
import com.meta.travel.entity.AlbumPhoto;
import com.meta.travel.entity.TravelAlbum;
import com.meta.travel.repository.AlbumPhotoRepository;
import com.meta.travel.repository.TravelAlbumRepository;
import com.meta.travel.util.DateUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 旅行相册服务（对应 lvxingxiangce.vue）
 */
@Service
@RequiredArgsConstructor
public class AlbumService {

    private final TravelAlbumRepository albumRepository;
    private final AlbumPhotoRepository photoRepository;

    @Transactional(readOnly = true)
    public List<AlbumVO> listByUser(Long userId) {
        return albumRepository.findByUserIdOrderByCreateTimeDesc(userId).stream()
                .map(this::toSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public AlbumVO getById(Long userId, Long albumId) {
        TravelAlbum album = requireOwned(userId, albumId);
        return toDetail(album);
    }

    /**
     * 新建或整体保存相册。albumId 为空则新建。
     */
    @Transactional
    public AlbumVO save(Long userId, Long albumId, AlbumRequest request) {
        if (CollectionUtils.isEmpty(request.getPhotos())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "请至少添加一张照片");
        }
        TravelAlbum album;
        if (albumId == null) {
            album = new TravelAlbum();
            album.setUserId(userId);
        } else {
            album = requireOwned(userId, albumId);
            // 覆盖式保存：先删除旧照片
            photoRepository.deleteByAlbumId(album.getId());
        }
        album.setTitle(StringUtils.hasText(request.getTitle()) ? request.getTitle() : "我的旅行相册");

        List<AlbumPhoto> photos = new ArrayList<>();
        int sort = 0;
        for (AlbumRequest.Photo p : request.getPhotos()) {
            AlbumPhoto photo = new AlbumPhoto();
            photo.setUrl(p.getUrl());
            photo.setCaption(p.getCaption());
            photo.setSortNo(sort++);
            photos.add(photo);
        }
        album.setCoverUrl(photos.get(0).getUrl());
        album = albumRepository.save(album);

        for (AlbumPhoto photo : photos) {
            photo.setAlbumId(album.getId());
        }
        photoRepository.saveAll(photos);

        return toDetail(album);
    }

    @Transactional
    public void delete(Long userId, Long albumId) {
        TravelAlbum album = requireOwned(userId, albumId);
        photoRepository.deleteByAlbumId(album.getId());
        albumRepository.delete(album);
    }

    private TravelAlbum requireOwned(Long userId, Long albumId) {
        TravelAlbum album = albumRepository.findByIdAndUserId(albumId, userId)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "相册不存在"));
        return album;
    }

    private AlbumVO toSummary(TravelAlbum album) {
        AlbumVO vo = new AlbumVO();
        vo.setId(album.getId());
        vo.setTitle(album.getTitle());
        vo.setCoverUrl(album.getCoverUrl());
        vo.setCreateTime(DateUtil.format(album.getCreateTime()));
        return vo;
    }

    private AlbumVO toDetail(TravelAlbum album) {
        AlbumVO vo = toSummary(album);
        List<AlbumVO.Photo> photos = photoRepository.findByAlbumIdOrderBySortNoAsc(album.getId()).stream()
                .map(p -> {
                    AlbumVO.Photo vp = new AlbumVO.Photo();
                    vp.setId(p.getId());
                    vp.setUrl(p.getUrl());
                    vp.setCaption(p.getCaption());
                    vp.setSortNo(p.getSortNo());
                    return vp;
                })
                .toList();
        vo.setPhotos(photos);
        return vo;
    }
}
