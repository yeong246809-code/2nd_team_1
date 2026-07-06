package org.example.k_market.repository;

import org.example.k_market.entity.CmMedia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CmMediaRepository extends JpaRepository<CmMedia, Integer> {
}
