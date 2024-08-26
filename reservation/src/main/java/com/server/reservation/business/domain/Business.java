package com.server.reservation.business.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.server.reservation.reservableitem.domain.ReservableItem;
import com.server.reservation.reservation.domain.Reservation;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@NoArgsConstructor
public class Business {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "business_id", nullable = false)
    @JsonProperty("businessId")
    private Long id;

    @Setter
    @Column(nullable = false)
    private String name;

    @Setter
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private BusinessType businessType;

    @JsonIgnore
    @OneToMany(mappedBy = "business")
    private List<ReservableItem> reservableItems;

    @JsonIgnore
    @OneToMany(mappedBy = "business")
    private List<Reservation> reservations;

    @Builder
    public Business(String name, BusinessType businessType) {
        this.name = name;
        this.businessType = businessType;
    }
}
