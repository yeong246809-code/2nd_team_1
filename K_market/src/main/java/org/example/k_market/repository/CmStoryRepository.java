package org.example.k_market.repository;

import org.example.k_market.entity.CmStory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CmStoryRepository extends JpaRepository<CmStory, Integer> {
}
