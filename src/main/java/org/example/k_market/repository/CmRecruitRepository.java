package org.example.k_market.repository;

import org.example.k_market.entity.CmRecruit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CmRecruitRepository extends JpaRepository<CmRecruit, Integer> {
    List<CmRecruit> findAllByOrderByIdDesc();   // ✅ List로 감싸야 함
}