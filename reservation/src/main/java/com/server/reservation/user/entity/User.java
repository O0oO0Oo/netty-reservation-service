package com.server.reservation.user.entity;

import com.server.reservation.reservationrecord.entity.ReservationRecord;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;


@Entity
@Getter
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private Long balance = 100_000_000L;

    @OneToMany(mappedBy = "user")
    private List<ReservationRecord> reservationRecords;

    @Builder
    public User(String name, Long balance) {
        this.name = name;
        this.balance = balance;
    }
}
