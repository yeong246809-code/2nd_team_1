package org.example.k_market.repository;

import org.example.k_market.entity.Faq;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FaqRepository extends JpaRepository<Faq, Integer> {

    List<Faq> findByType1OrderByNoAsc(String type1);
}