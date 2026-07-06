package org.example.k_market.repository;

import org.example.k_market.entity.Version;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VersionRepository extends JpaRepository<Version, Long> {
    // 작성자(member)를 한 번에 조인해서 가져옴 (성능 향상)
    @Query("select v from Version v join fetch v.users")
    List<Version> findAllWithMember();
}
