package com.server.reservation.reservableitem.entity;

import com.server.reservation.common.entity.BaseTimeEntity;
import com.server.reservation.reservationrecord.entity.ReservationRecord;
import com.server.reservation.business.entity.Business;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
public class ReservableItem extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id", nullable = false)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Setter
    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private int maxQuantityPerUser;

    @Temporal(TemporalType.TIMESTAMP)
    private Date reservableTime;

    @Setter
    @Column(nullable = false)
    private Long price = 0L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @OneToMany(mappedBy = "reservableItem")
    private List<ReservationRecord> reservationRecord;

    @Builder
    public ReservableItem(String name, int quantity, int maxQuantityPerUser, Date reservableTime, Long price, Business business) {
        this.name = name;
        this.quantity = quantity;
        this.maxQuantityPerUser = maxQuantityPerUser;
        this.reservableTime = reservableTime;
        this.price = price;
        this.business = business;
    }
}