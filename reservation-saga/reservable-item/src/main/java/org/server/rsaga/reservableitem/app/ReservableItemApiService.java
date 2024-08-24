package org.server.rsaga.reservableitem.app;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.server.rsaga.common.domain.ForeignKey;
import org.server.rsaga.common.domain.Money;
import org.server.rsaga.reservableitem.domain.ReservableItem;
import org.server.rsaga.reservableitem.domain.ReservableTime;
import org.server.rsaga.reservableitem.domain.Stock;
import org.server.rsaga.reservableitem.dto.request.*;
import org.server.rsaga.reservableitem.dto.response.ReservableItemDetailsResponse;
import org.server.rsaga.reservableitem.dto.response.ReservableItemWithTimeDetailsResponse;
import org.server.rsaga.reservableitem.infra.repository.ReservableItemCustomRepository;
import org.server.rsaga.reservableitem.infra.repository.ReservableItemJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservableItemApiService {
    private final ReservableItemCustomRepository reservableItemCustomRepository;
    private final ReservableItemJpaRepository reservableItemJpaRepository;

    // todo paging
    @Transactional(readOnly = true)
    public List<ReservableItemDetailsResponse> findReservableItemList() {
        return ReservableItemDetailsResponse.of(reservableItemJpaRepository.findAll());
    }

    @Transactional(readOnly = true)
    public ReservableItemWithTimeDetailsResponse findReservableItem(Long reservableItemId) {
        return ReservableItemWithTimeDetailsResponse.of(
                reservableItemCustomRepository.findReservableItemOrElseThrow(reservableItemId)
        );
    }

    @Transactional
    public ReservableItemWithTimeDetailsResponse registerReservableItem(RegisterReservableItemRequest request) {
        List<ReservableTime> reservableTimes = new ArrayList<>();
        for (RegisterReservableTime registerReservableTime : request.reservableTimes()) {
            Stock stock = new Stock(
                    registerReservableTime.stockQuantity(),
                    registerReservableTime.stockUnit()
            );

            ReservableTime reservableTime = new ReservableTime(
                    registerReservableTime.reservableTime(),
                    stock,
                    registerReservableTime.isTimeAvailable()

            );
            reservableTimes.add(reservableTime);
        }
        Money price = new Money(request.price());

        // 생성
        ReservableItem reservableItem = new ReservableItem(
                request.name(),
                request.maxQuantityPerUser(),
                reservableTimes,
                price,
                new ForeignKey(request.businessId()),
                request.isItemAvailable()
        );
        return ReservableItemWithTimeDetailsResponse.of(
                reservableItemJpaRepository.save(reservableItem)
        );
    }

    @Transactional
    public ReservableItemWithTimeDetailsResponse modifyReservableItem(Long reservableItemId, ModifyReservableItemRequest request) {
        Long businessId = request.businessId();

        ModifyReservableTime reservableTimeDto = request.reservableTime();

        ReservableItem reservableItem =
                reservableItemCustomRepository.findReservableItemByIdAndBusinessIdOrElseThrow(
                        reservableItemId,
                        new ForeignKey(businessId)
                );

        reservableItem
                .changeName(
                        request.name()
                )
                .changeMaxQuantityPerUser(
                        request.maxQuantityPerUser()
                )
                .changePrice(
                        new Money(request.price())
                )
                .changeIsItemAvailable(
                        request.isItemAvailable()
                )
                .updateReservableTime(
                        reservableTimeDto.reservableTimeId(),
                        reservableTimeDto.reservableTime(),
                        new Stock(
                                reservableTimeDto.stockQuantity(),
                                reservableTimeDto.stockUnit()
                        ),
                        reservableTimeDto.isTimeAvailable()
                );

        return ReservableItemWithTimeDetailsResponse.of(reservableItem);
    }

    @Transactional
    public void deleteReservableItem(Long reservableItemId, DeleteReservableItemRequest request) {
        Long businessId = request.businessId();

        ReservableItem reservableItem = reservableItemCustomRepository.findReservableItemByIdAndBusinessIdOrElseThrow(
                reservableItemId,
                new ForeignKey(businessId)
        );
        reservableItem.makeUnavailable();
    }
}
