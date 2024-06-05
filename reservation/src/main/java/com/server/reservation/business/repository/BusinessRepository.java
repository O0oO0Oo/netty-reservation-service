package com.server.reservation.business.repository;

import com.server.reservation.business.entity.Business;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BusinessRepository extends JpaRepository<Business, Long> {
}
