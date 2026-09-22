package com.meta.travel.repository;

import com.meta.travel.entity.TravelAlbum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TravelAlbumRepository extends JpaRepository<TravelAlbum, Long> {

    List<TravelAlbum> findByUserIdOrderByCreateTimeDesc(Long userId);

    Optional<TravelAlbum> findByIdAndUserId(Long id, Long userId);
}
