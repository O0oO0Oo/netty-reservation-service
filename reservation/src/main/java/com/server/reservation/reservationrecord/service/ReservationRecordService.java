package com.server.reservation.reservationrecord.service;

import com.server.reservation.business.entity.Business;
import com.server.reservation.business.service.BusinessService;
import com.server.reservation.common.exception.CustomException;
import com.server.reservation.common.exception.ErrorCode;
import com.server.reservation.reservableitem.entity.ReservableItem;
import com.server.reservation.reservableitem.service.ReservableItemService;
import com.server.reservation.reservationrecord.dto.ModifyReservationRecordRequest;
import com.server.reservation.reservationrecord.dto.RegisterReservationRecordRequest;
import com.server.reservation.reservationrecord.entity.ReservationRecord;
import com.server.reservation.reservationrecord.entity.ReservationRecordStatus;
import com.server.reservation.reservationrecord.repository.ReservationRecordRepository;
import com.server.reservation.user.entity.User;
import com.server.reservation.user.service.UserService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ReservationRecordService {
    private final ReservationRecordRepository reservationRecordRepository;
    private final UserService userService;
    private final BusinessService businessService;
    private final ReservableItemService reservableItemService;
    private final Validator validator;

    public List<ReservationRecord> findReservationRecordList(Long userId) {
        // TODO : Graph 로
        userService.findUser(userId);
        return reservationRecordRepository.findByUserId(userId);
    }

    @Transactional
    public ReservationRecord findReservationRecord(Long userId, Long reservationId) {
        return findReservationRecordOrElseThrow(reservationId, userId);
    }

    private ReservationRecord findReservationRecordOrElseThrow(Long reservationId, Long userId) {
        return reservationRecordRepository.findByIdAndUserId(reservationId, userId)
                .orElseThrow(
                        () -> new CustomException(ErrorCode.RESERVATION_RECORD_NOT_FOUND)
                );
    }

    // TODO : 프로젝트에서 중요한 부분
    @Transactional
    public ReservationRecord registerReservationRecord(Long userId, RegisterReservationRecordRequest request) {
        User user = userService.findUser(userId);
        Business business = businessService.findBusiness(request.businessId());

        // read-only 안됨, 수정하고 저장해야함.
        ReservableItem item = reservableItemService.findBusinessReservableItemNotReadOnly(request.itemId(), request.businessId());

        // item 이 예약 가능한지.
        isItemIsAvailable(item.getIsAvailable());

        // 예약 날짜가 지났는지
        isDatePassed(item.getReservableTime());

        // 해당 아이템에 대한 구매기록을 확인하여 인당 구매 가능 수 초과하는지.
        checkQuantityPerMax(item, user, request.quantity());

        // 남앙있는 수량은 구매 수량보다 많아야 함.
        // 변경하는 순간이 아닌 엔티티의 유효성 검사가 진행될 때, 커밋같은? @Min 검사가 발생한다. -> RollBackException
        // 미리 검사를 하여 트랜잭션을 멈춘다면, 불필요한 데이터 베이스 작업 피할 수 있다.
        item.setQuantity(
                item.getQuantity() - request.quantity()
        );
        checkValidation(item);

        // user 잔액 감소
        user.setBalance(
                user.getBalance() - (item.getPrice() * request.quantity())
        );
        checkValidation(user);

        ReservationRecord reservation = ReservationRecord.builder()
                .user(user)
                .business(business)
                .reservableItem(item)
                .quantity(request.quantity())
                .reservationRecordStatus(ReservationRecordStatus.RESERVED)
                .build();

        return reservationRecordRepository.save(reservation);
    }

    private void isItemIsAvailable(Boolean isAvailable) {
        if (!isAvailable) {
            throw new CustomException(ErrorCode.RESERVATION_ITEM_IS_NOT_AVAILABLE);
        }
    }

    private void checkQuantityPerMax(ReservableItem item, User user, Long requestQuantity) {
        // 이전 구매기록이 없을 경우 0
        Integer quantity =
                Objects.requireNonNullElse(
                        reservationRecordRepository.findSumQuantityByUserIdAndReservableItemIdAndReserved(
                                item.getId(), user.getId(), ReservationRecordStatus.RESERVED
                        ), 0
                );
        
        // 구매 기록이 없으면 실행 x
        if (item.getMaxQuantityPerUser() < quantity + requestQuantity) {
            throw new CustomException(ErrorCode.MAX_QUANTITY_EXCEEDED);
        }
    }

    private <T >void checkValidation(T object) {
        Set<ConstraintViolation<T>> violations = validator.validate(object);
        if(!violations.isEmpty()){
            throw new ConstraintViolationException(violations);
        }
    }

    @Transactional
    public ReservationRecord modifyReservationRecord(Long userId, Long reservationId, ModifyReservationRecordRequest request) {
        // 예약 날짜 같이 가져오기.
        ReservationRecord reservation = findWithReservableItemAndUserByIdAndUserIdOrElseThrow(reservationId, userId);
        User user = reservation.getUser();
        ReservableItem item = reservation.getReservableItem();

        // 변경하려는 값이 같다면 변경 불가.
        isSameStatus(reservation.getReservationRecordStatus(), request.reservationRecordStatus());

        // 이미 취소, 완료면, 변경 불가.
        isAlreadyCanceledOrCompleted(reservation);

        // 날짜가 지났다면 취소 불가.
        isDatePassedWhenCanceled(reservation, request.reservationRecordStatus());

        reservation
                .setReservationRecordStatus(
                        request.reservationRecordStatus()
                );

        // 취소라면 아이템 수량 복구
        ifCanceledAddReservationItemQuantity(reservation.getReservableItem(), reservation);

        // 취소라면 유저 잔액 복구
        ifCanceledAddUserBalance(user, reservation.getQuantity(), item.getPrice());

        return reservation;
    }

    private void isSameStatus(ReservationRecordStatus origin, ReservationRecordStatus dest) {
        if (origin.equals(dest)) {
            throw new CustomException(ErrorCode.SAME_STATUS);
        }
    }

    private ReservationRecord findWithReservableItemAndUserByIdAndUserIdOrElseThrow(Long reservationId, Long userId) {
        return reservationRecordRepository.findWithReservableItemAndUserByIdAndUserId(reservationId, userId)
                .orElseThrow(
                        () -> new CustomException(ErrorCode.ITEM_NOT_FOUND)
                );
    }

    private void isAlreadyCanceledOrCompleted(ReservationRecord reservation) {
        ReservationRecordStatus status = reservation.getReservationRecordStatus();
        if (status.equals(ReservationRecordStatus.CANCELED) || status.equals(ReservationRecordStatus.COMPLETED)
        ) {
            throw new CustomException(ErrorCode.ALREADY_CANCELED_OR_COMPLETED);
        }
    }

    private void isDatePassedWhenCanceled(ReservationRecord reservation, ReservationRecordStatus requestStatus) {
        if(requestStatus.equals(ReservationRecordStatus.CANCELED)) {
            isDatePassed(
                    reservation.getReservableItem()
                            .getReservableTime()
            );
        }
    }

    private void isDatePassed(Date date) {
        if (Objects.nonNull(date) && date.before(new Date())) {
            throw new CustomException(ErrorCode.RESERVATION_DATE_PASSED);
        }
    }

    private void ifCanceledAddReservationItemQuantity(ReservableItem item, ReservationRecord reservation) {
        if (reservation.getReservationRecordStatus().equals(ReservationRecordStatus.CANCELED)) {
            item.setQuantity(
                    item.getQuantity() + reservation.getQuantity()
            );
        }
    }

    private void ifCanceledAddUserBalance(User user, Long reservationQuantity, Long itemPrice) {
        user.setBalance(
                user.getBalance() + reservationQuantity * itemPrice
        );
    }
}
