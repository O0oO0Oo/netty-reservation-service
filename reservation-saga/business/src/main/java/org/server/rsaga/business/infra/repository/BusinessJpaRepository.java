package org.server.rsaga.business.infra.repository;

import org.server.rsaga.business.domain.Business;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BusinessJpaRepository extends JpaRepository<Business, Long> {
    boolean existsByIdAndClosedFalse(Long id);
    Optional<Business> findByIdAndClosedFalse(Long id);
}