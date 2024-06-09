package com.server.reservation.user.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.server.reservation.reservationrecord.entity.ReservationRecord;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;


@Entity
@Getter
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false)
    @JsonProperty("userId")
    private Long id;

    @Setter
    @Column(nullable = false, unique = true)
    private String name;

    @Setter
    @Min(value = 0, message = "잔고는 0 이상이어야 합니다.")
    @Column(nullable = false)
    private Long balance;

    @JsonIgnore
    @OneToMany(mappedBy = "user")
    private List<ReservationRecord> reservationRecords;

    @Builder
    public User(String name, Long balance) {
        this.name = name;
        this.balance = balance;
    }
}
