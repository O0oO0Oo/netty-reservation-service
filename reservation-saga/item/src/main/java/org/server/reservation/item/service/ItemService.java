package org.server.reservation.item.service;

import lombok.RequiredArgsConstructor;
import org.server.reservation.core.common.exception.CustomException;
import org.server.reservation.core.common.exception.ErrorCode;
import org.server.reservation.item.dto.ModifyItemRequest;
import org.server.reservation.item.dto.RegisterItemRequest;
import org.server.reservation.item.entity.Item;
import org.server.reservation.item.repository.ItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ItemService {
    private final ItemRepository itemRepository;
    // TODO : 의존
//    private final BusinessService businessService;

    @Transactional(readOnly = true)
    public List<Item> findBusinessReservableItemList(Long businessId) {
        return itemRepository.findByBusinessId(businessId);
    }

    @Transactional
    public Item registerBusinessReservableItem(Long businessId, RegisterItemRequest request) {
        // business id
        // TODO : business id 받아오기 
//        Business business = businessService.findBusiness(businessId);

        // 예약가능 시간이 현재 시간보다 앞일 경우 종료
        isDateLaterToday(request.reservableTime());

        Item reservableItem = Item.builder()
                // TODO : business id 삽입
                .businessId(0L)
                .name(request.name())
                .quantity(request.quantity())
                .price(request.price())
                .maxQuantityPerUser(request.maxQuantityPerUser())
                .reservableTime(request.reservableTime())
                .isAvailable(true)
                .build();

        return itemRepository.save(reservableItem);
    }

    private void isDateLaterToday(Date date) {
        if (Objects.nonNull(date) && date.before(new Date(System.currentTimeMillis()))) {
            throw new CustomException(ErrorCode.RESERVATION_DATE_LATER_TODAY);
        }
    }

    @Transactional(readOnly = true)
    public Item findBusinessReservableItem(Long itemId, Long businessId) {
        return findBusinessReservableItemOrElseThrow(itemId, businessId);
    }

    @Transactional
    public Item findBusinessReservableItemNotReadOnly(Long itemId, Long businessId) {
        return findBusinessReservableItemOrElseThrow(itemId, businessId);
    }

    private Item findBusinessReservableItemOrElseThrow(Long itemId, Long businessId) {
        return itemRepository.findByIdAndBusinessId(itemId, businessId)
                .orElseThrow(
                        () -> new CustomException(ErrorCode.ITEM_NOT_FOUND)
                );
    }

    @Transactional(readOnly = true)
    public List<Item> findReservableItemList() {
        return itemRepository.findAll();
    }

    @Transactional
    public Item modifyBusinessReservableItem(Long itemId, Long businessId, ModifyItemRequest request) {
        Item item = findBusinessReservableItemOrElseThrow(itemId, businessId);

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
        Item item = findBusinessReservableItem(itemId, businessId);
        item.setIsAvailable(false);
    }
}
