package org.example.k_market.repository;

import org.example.k_market.dto.VersionDTO;
import org.example.k_market.entity.Version;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VersionRepository extends JpaRepository<Version, Long> {
    @Query("SELECT new org.example.k_market.dto.VersionDTO(v.id, v.versionCode, v.memberNo, u.id, v.createdAt, v.changeLog) " +
            "FROM Version v LEFT JOIN Users u ON v.memberNo = u.memberNo " +
            "ORDER BY v.createdAt DESC")
    List<VersionDTO> findAllVersionsWithUserId();
}
