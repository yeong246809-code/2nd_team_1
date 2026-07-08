package org.example.k_market.repository;

import org.example.k_market.entity.Grade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface GradeRepository extends JpaRepository<Grade, Integer> {
    Optional<Grade> findByName(String name);
    boolean existsByName(String name);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Grade g SET g.rewardRate = :rewardRate WHERE g.name = :name")
    void updateRewardRateByName(@Param("name") String name, @Param("rewardRate") BigDecimal rewardRate);
}
