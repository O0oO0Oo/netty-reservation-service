package com.server.reservation.reservableitem.service;

import com.server.reservation.business.domain.Business;
import com.server.reservation.business.service.BusinessService;
import com.server.reservation.common.exception.CustomException;
import com.server.reservation.common.exception.ErrorCode;
import com.server.reservation.reservableitem.dto.ModifyReservableItemRequest;
import com.server.reservation.reservableitem.dto.RegisterReservableItemRequest;
import com.server.reservation.reservableitem.domain.ReservableItem;
import com.server.reservation.reservableitem.repository.ReservableItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ReservableItemService {
    private final ReservableItemRepository reservableItemRepository;
    private final BusinessService businessService;

    @Transactional(readOnly = true)
    public List<ReservableItem> findBusinessReservableItemList(Long businessId) {
        return reservableItemRepository.findByBusinessId(businessId);
    }

    @Transactional
    public ReservableItem registerBusinessReservableItem(Long businessId, RegisterReservableItemRequest request) {
        Business business = businessService.findBusiness(businessId);

        // 예약가능 시간이 현재 시간보다 앞일 경우 종료
        isDateLaterToday(request.reservableTime());

        ReservableItem reservableItem = ReservableItem.builder()
                .business(business)
                .name(request.name())
                .quantity(request.quantity())
                .price(request.price())
                .maxQuantityPerUser(request.maxQuantityPerUser())
                .reservableTime(request.reservableTime())
                .isAvailable(true)
                .build();

        return reservableItemRepository.save(reservableItem);
    }

    private void isDateLaterToday(Date date) {
        if (Objects.nonNull(date) && date.before(new Date(System.currentTimeMillis()))) {
            throw new CustomException(ErrorCode.RESERVATION_DATE_LATER_TODAY);
        }
    }

    @Transactional(readOnly = true)
    public ReservableItem findBusinessReservableItem(Long itemId, Long businessId) {
        return findBusinessReservableItemOrElseThrow(itemId, businessId);
    }

    @Transactional
    public ReservableItem findBusinessReservableItemNotReadOnly(Long itemId, Long businessId) {
        return findBusinessReservableItemOrElseThrow(itemId, businessId);
    }

    private ReservableItem findBusinessReservableItemOrElseThrow(Long itemId, Long businessId) {
        return reservableItemRepository.findByIdAndBusinessId(itemId, businessId)
                .orElseThrow(
                        () -> new CustomException(ErrorCode.ITEM_NOT_FOUND)
                );
    }

    @Transactional(readOnly = true)
    public List<ReservableItem> findReservableItemList() {
        return reservableItemRepository.findAll();
    }

    @Transactional
    public ReservableItem modifyBusinessReservableItem(Long itemId, Long businessId, ModifyReservableItemRequest request) {
        ReservableItem item = findBusinessReservableItemOrElseThrow(itemId, businessId);

        // 수정하려고 하는 날짜가 오늘보다 뒤여야함.
        isDateLaterToday(request.reservableTime());

        // isAvailable 판매 종료되어 변경 불가능
        isItemAvailable(item.getIsAvailable());

        item
                .setQuantity(
                        requireNonNullElse(request.quantity(), item.getQuantity())
                );

        item
                .setName(
                        requireNonNullElse(request.name(), item.getName())
                );
        item
                .setMaxQuantityPerUser(
                        requireNonNullElse(request.maxQuantityPerUser(), item.getMaxQuantityPerUser())
                );
        item
                .setPrice(
                        requireNonNullElse(request.price(), item.getPrice())
                );
        item
                .setReservableTime(
                        requireNonNullElse(request.reservableTime(), item.getReservableTime())
                );

        return item;
    }

    private void isItemAvailable(Boolean isAvailable) {
        if (!isAvailable) {
            throw new CustomException(ErrorCode.ITEM_IS_NOT_AVAILABLE);
        }
    }

    private <T> T requireNonNullElse(T dest, T origin) {
        return Objects.requireNonNullElse(dest, origin);
    }

    @Transactional
    public void deleteBusinessReservableItem(Long itemId, Long businessId) {
        ReservableItem item = findBusinessReservableItem(itemId, businessId);
        item.setIsAvailable(false);
    }
}
