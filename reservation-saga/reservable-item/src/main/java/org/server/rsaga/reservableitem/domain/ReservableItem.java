package org.server.rsaga.reservableitem.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.server.rsaga.common.domain.BaseTime;
import org.server.rsaga.common.domain.ForeignKey;
import org.server.rsaga.common.domain.Money;
import org.server.rsaga.common.event.BusinessValidationEvent;
import org.server.rsaga.common.event.EventPublisher;
import org.server.rsaga.common.exception.CustomException;
import org.server.rsaga.common.exception.ErrorCode;

import java.util.Date;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReservableItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservable_item_id", nullable = false)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Long maxQuantityPerUser;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "price", nullable = false))
    private Money price;

    @OneToMany(mappedBy = "reservableItem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReservableTime> reservableTimes;

    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "business_id", nullable = false))
    private ForeignKey businessId;

    @Column(nullable = false)
    private boolean isItemAvailable;

    @Embedded
    private BaseTime baseTime;

    public ReservableItem(
            final String name,
            final Long maxQuantityPerUser,
            final List<ReservableTime> reservableTimes,
            final Money price,
            final ForeignKey businessId,
            final boolean isItemAvailable) {
        checkName(name);
        this.name = name;

        checkQuantityPerUser(maxQuantityPerUser);
        this.maxQuantityPerUser = maxQuantityPerUser;

        checkReservableTimes(reservableTimes);
        for (ReservableTime reservableTime : reservableTimes) {
            reservableTime.assignReservableItem(this);
        }
        this.reservableTimes = reservableTimes;

        checkPrice(price);
        this.price = price;

        validateBusinessId(businessId);
        this.businessId = businessId;

        this.isItemAvailable = isItemAvailable;
    }

    /**
     * ---------------------- getter ----------------------
     */
    public Long getPrice() {
        return this.price.getAmount();
    }

    public boolean isItemAvailable() {
        return isItemAvailable;
    }

    public boolean isTimeAvailable(Long timeId) {
        boolean isTimeAvailable = false;
        for (ReservableTime reservableTime : reservableTimes) {
            if (reservableTime.getId().equals(timeId)) {
                isTimeAvailable = reservableTime.isTimeAvailable();
                break;
            }
        }
        return isItemAvailable() && isTimeAvailable;
    }

    public long getBusinessId() {
        return businessId.getId();
    }

    /**
     * ---------------------- setter ----------------------
     */
    public ReservableItem changeName(String newName) {
        if (newName != null && !newName.trim().isEmpty()) {
            this.name = newName;
        }
        return this;
    }

    public ReservableItem changeMaxQuantityPerUser(Long maxQuantityPerUser) {
        if (maxQuantityPerUser != null) {
            this.maxQuantityPerUser = maxQuantityPerUser;
        }
        return this;
    }

    public ReservableItem changePrice(Money newPrice) {
        if (newPrice != null) {
            this.price = newPrice;
        }
        return this;
    }

    public void increaseReservableTimeStock(Long timeId, Long requestQuantity) {
        ReservableTime reservableTime = reservableTimes.stream()
                .filter(time -> time.getId().equals(timeId))
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVABLE_TIME_NOT_FOUND));

        // Stock 증가
        reservableTime.increaseStock(requestQuantity);
    }

    public void decreaseReservableTimeStock(Long timeId, Long requestQuantity) {
        ReservableTime reservableTime = reservableTimes.stream()
                .filter(time -> time.getId().equals(timeId))
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVABLE_TIME_NOT_FOUND));

        // Stock 감소
        reservableTime.decreaseStock(requestQuantity);
    }

    /**
     * false 라면 @makeUnavailable 이 실행된다.
     */
    public ReservableItem changeIsItemAvailable(Boolean newIsItemAvailable) {
        if (newIsItemAvailable != null) {
            if (!newIsItemAvailable) {
                makeUnavailable();
            }
            this.isItemAvailable = newIsItemAvailable;
        }
        return this;
    }

    /**
     * 상품, 포함된 모든 예약 가능 시간을 사용 불가능으로 설정
     */
    public void makeUnavailable() {
        this.isItemAvailable = false;
        for (ReservableTime reservableTime : reservableTimes) {
            reservableTime.changeIsTimeAvailable(false);
        }
    }

    /**
     * ---------------------- ReservableTime setter ----------------------
     */
    public ReservableItem addReservableTime(ReservableTime newReservableTime) {
        if (newReservableTime != null) {
            newReservableTime.assignReservableItem(this);
            reservableTimes.add(newReservableTime);
        }
        return this;
    }

    public ReservableItem removeReservableTime(ReservableTime reservableTime) {
        if (reservableTime != null) {
            reservableTime.changeIsTimeAvailable(false);
        }
        return this;
    }

    public ReservableItem updateReservableTime(Long timeId, Date newDate, Stock newStock, Boolean isTimeAvailable) {
        for (ReservableTime time : reservableTimes) {
            if (time.getId().equals(timeId)) {
                time.adjustStock(newStock);
                time.rescheduleDate(newDate);
                time.changeIsTimeAvailable(isTimeAvailable);
                break;
            }
        }
        return this;
    }

    /**
     * ---------------------- validation ----------------------
     */
    private void checkName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Reservable-Item name cannot be empty.");
        }

        if (name.length() > 100) {
            throw new IllegalArgumentException("Reservable-Item name cannot exceed 100 characters.");
        }
    }

    private void checkQuantityPerUser(Long maxQuantityPerUser) {
        if (maxQuantityPerUser == null || maxQuantityPerUser < 0) {
            throw new IllegalArgumentException("The maxQuantityPerUser should not be a negative value.");
        }
    }

    private void checkReservableTimes(List<ReservableTime> reservableTimes) {
        if (reservableTimes == null || reservableTimes.isEmpty()) {
            throw new IllegalArgumentException("The list of reservable times should contain at least one reservable time.");
        }
    }

    private void checkPrice(Money price) {
        if (price == null) {
            throw new IllegalArgumentException("The price should not be null.");
        }
    }

    /**
     * {@link BusinessValidationEvent} 를 발행하여 회사가 존재하는지, 폐업 상태인지 검증한다.
     */
    private void validateBusinessId(ForeignKey businessId) {
        if (businessId == null) {
            throw new IllegalArgumentException("the businessId should not be null.");
        }
        EventPublisher.publish(new BusinessValidationEvent(businessId.getId()));
    }

    public void validateRequestQuantity(long requestQuantity) {
        if (requestQuantity > this.maxQuantityPerUser) {
            throw new IllegalArgumentException("Requested quantity exceeds the maximum allowed per user.");
        }
    }

    public boolean checkRequestQuantityLowerThenLimit(long requestQuantity) {
        return this.maxQuantityPerUser >= requestQuantity;
    }
}