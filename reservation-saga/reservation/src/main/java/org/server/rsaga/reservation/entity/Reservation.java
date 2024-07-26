package org.server.rsaga.reservation.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.server.rsaga.common.entity.BaseTimeEntity;

@Entity
@Getter
@NoArgsConstructor
public class Reservation extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservation_id", nullable = false)
    @JsonProperty("reservationId")
    private Long id;

    @Column(name = "business_id", nullable = false)
    private Long businessId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "item_Id", nullable = false)
    private Long itemId;

    @Min(value = 1, message = "구매 수량은 1 이상이어야 합니다.")
    @Column(nullable = false)
    private Long quantity;

    @Setter
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ReservationStatus reservationStatus;

    @Builder
    public Reservation(Long businessId, Long userId, Long itemId, Long quantity, ReservationStatus reservationStatus) {
        this.businessId = businessId;
        this.userId = userId;
        this.itemId = itemId;
        this.quantity = quantity;
        this.reservationStatus = reservationStatus;
    }
}