package com.server.reservation.business.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.server.reservation.reservableitem.entity.ReservableItem;
import com.server.reservation.reservationrecord.entity.ReservationRecord;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@Immutable // An immutable entity need not be dirty-checked, and so Hibernate does not need to maintain a snapshot of its state. This may help reduce memory allocation
public class Business {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "business_id", nullable = false)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private BusinessType businessType;

    @JsonIgnore
    @OneToMany(mappedBy = "business")
    private List<ReservableItem> reservableItems;

    @JsonIgnore
    @OneToMany(mappedBy = "business")
    private List<ReservationRecord> reservationRecords;

    @Builder
    public Business(String name, BusinessType businessType) {
        this.name = name;
        this.businessType = businessType;
    }
}
