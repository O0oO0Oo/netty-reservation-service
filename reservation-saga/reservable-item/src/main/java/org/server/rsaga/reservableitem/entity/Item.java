package org.server.rsaga.reservableitem.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.server.rsaga.common.entity.BaseTimeEntity;

import java.util.Date;

@Entity
@Getter
@NoArgsConstructor
public class Item extends BaseTimeEntity {
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

    @JsonProperty("businessId")
    @JoinColumn(name = "business_id", nullable = false)
    private Long businessId;

    @Setter
    private Boolean isAvailable;

    @Builder
    public Item(String name, Long quantity, Long maxQuantityPerUser, Date reservableTime, Long price, Long businessId, boolean isAvailable) {
        this.name = name;
        this.quantity = quantity;
        this.maxQuantityPerUser = maxQuantityPerUser;
        this.reservableTime = reservableTime;
        this.price = price;
        this.businessId = businessId;
        this.isAvailable = isAvailable;
    }
}