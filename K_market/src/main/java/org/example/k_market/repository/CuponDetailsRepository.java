package org.example.k_market.repository;

import org.example.k_market.entity.CuponDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CuponDetailsRepository extends JpaRepository<CuponDetails, Long> {
}
