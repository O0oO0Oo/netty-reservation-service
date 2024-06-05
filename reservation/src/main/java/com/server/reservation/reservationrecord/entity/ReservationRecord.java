package com.server.reservation.reservationrecord.entity;

import com.server.reservation.common.entity.BaseTimeEntity;
import com.server.reservation.reservableitem.entity.ReservableItem;
import com.server.reservation.business.entity.Business;
import com.server.reservation.user.entity.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@NoArgsConstructor
public class ReservationRecord extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "record_id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id")
    private Business business;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private ReservableItem reservableItem;

    private int quantity = 0;

    @Setter
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ReservationRecordStatus reservationRecordStatus;

    @Builder
    public ReservationRecord(Business business, User user, ReservableItem reservableItem, int quantity, ReservationRecordStatus reservationRecordStatus) {
        this.business = business;
        this.user = user;
        this.reservableItem = reservableItem;
        this.quantity = quantity;
        this.reservationRecordStatus = reservationRecordStatus;
    }
}
