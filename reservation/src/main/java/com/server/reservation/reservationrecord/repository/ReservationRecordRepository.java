package com.server.reservation.reservationrecord.repository;

import com.server.reservation.reservationrecord.entity.ReservationRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRecordRepository extends JpaRepository<ReservationRecord, Long> {
}
