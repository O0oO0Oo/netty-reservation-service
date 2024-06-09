package com.server.reservation.reservationrecord.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.server.reservation.business.entity.Business;
import com.server.reservation.common.entity.BaseTimeEntity;
import com.server.reservation.reservableitem.entity.ReservableItem;
import com.server.reservation.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
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
    @JsonProperty("reservationId")
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id")
    private Business business;

    @Transient
    @JsonProperty("businessId")
    private Long transientBusinessId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Transient
    @JsonProperty("userId")
    private Long transientUserId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private ReservableItem reservableItem;

    @Transient
    @JsonProperty("itemId")
    private Long transientReservableItemId;

    @Min(value = 1, message = "구매 수량은 1 이상이어야 합니다.")
    @Column(nullable = false)
    private Long quantity;

    @Setter
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ReservationRecordStatus reservationRecordStatus;

    @Builder
    public ReservationRecord(Business business, User user, ReservableItem reservableItem, Long quantity, ReservationRecordStatus reservationRecordStatus) {
        this.business = business;
        this.user = user;
        this.reservableItem = reservableItem;
        this.quantity = quantity;
        this.reservationRecordStatus = reservationRecordStatus;
    }

    @PostLoad  // load
    @PrePersist // new save
    public void setForeignIds() {
        this.transientBusinessId = this.business.getId();
        this.transientUserId = this.user.getId();
        this.transientReservableItemId = this.reservableItem.getId();
    }
}
