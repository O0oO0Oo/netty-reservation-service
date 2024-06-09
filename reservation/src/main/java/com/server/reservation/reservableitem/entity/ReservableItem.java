package com.server.reservation.reservableitem.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.server.reservation.business.entity.Business;
import com.server.reservation.common.entity.BaseTimeEntity;
import com.server.reservation.reservationrecord.entity.ReservationRecord;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
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
    @JsonProperty("itemId")
    private Long id;

    @Setter
    @Column(nullable = false)
    private String name;

    @Setter
    @Min(value = 0, message = "남은 수량은 0 이상이어야 합니다.")
    @Column(nullable = false)
    private Long quantity;

    @Setter
    @Min(value = 1, message = "최대 구매 수량은 1 이상이어야 합니다.")
    @Column(nullable = false)
    private Long maxQuantityPerUser;

    @Setter
    @Temporal(TemporalType.TIMESTAMP)
    private Date reservableTime;

    @Setter
    @Min(value = 0, message = "가격은 0 이상이어야 합니다.")
    @Column(nullable = false)
    private Long price = 0L;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @Transient
    @JsonProperty("businessId")
    private Long transientBusinessId;

    @Setter
    private Boolean isAvailable;

    @JsonIgnore
    @OneToMany(mappedBy = "reservableItem")
    private List<ReservationRecord> reservationRecord;

    @Builder
    public ReservableItem(String name, Long quantity, Long maxQuantityPerUser, Date reservableTime, Long price, Business business, boolean isAvailable) {
        this.name = name;
        this.quantity = quantity;
        this.maxQuantityPerUser = maxQuantityPerUser;
        this.reservableTime = reservableTime;
        this.price = price;
        this.business = business;
        this.isAvailable = isAvailable;
    }

    @PostLoad // load
    @PrePersist // save
    public void prePersist() {
        this.transientBusinessId = this.business.getId();
    }
}