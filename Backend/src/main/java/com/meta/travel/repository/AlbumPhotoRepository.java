package com.meta.travel.repository;

import com.meta.travel.entity.AlbumPhoto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlbumPhotoRepository extends JpaRepository<AlbumPhoto, Long> {

    List<AlbumPhoto> findByAlbumIdOrderBySortNoAsc(Long albumId);

    void deleteByAlbumId(Long albumId);
}
