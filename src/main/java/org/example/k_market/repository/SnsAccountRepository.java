package org.example.k_market.repository;

import org.example.k_market.entity.SnsAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface SnsAccountRepository extends JpaRepository<SnsAccount, Integer> {
    Optional<SnsAccount> findByProviderAndProviderId(String provider, String providerId);
    List<SnsAccount> findAllByMemberNo(int memberNo);
}
