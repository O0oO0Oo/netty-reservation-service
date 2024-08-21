package org.server.rsaga.reservation.app;

import lombok.RequiredArgsConstructor;
import org.server.rsaga.common.domain.ForeignKey;
import org.server.rsaga.reservation.dto.request.FindReservationRequest;
import org.server.rsaga.reservation.dto.response.ReservationDetailsResponse;
import org.server.rsaga.reservation.infra.repository.ReservationCustomRepository;
import org.server.rsaga.reservation.infra.repository.ReservationJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationApiService {
    private final ReservationJpaRepository reservationJpaRepository;
    private final ReservationCustomRepository reservationCustomRepository;

    @Transactional(readOnly = true)
    public List<ReservationDetailsResponse> findReservationList(FindReservationRequest request) {
        return ReservationDetailsResponse.of(
                reservationJpaRepository.findByUserId(
                        new ForeignKey(request.userId())
                )
        );
    }

    @Transactional(readOnly = true)
    public ReservationDetailsResponse findReservation(FindReservationRequest request, Long reservationId) {
        return ReservationDetailsResponse.of(
                reservationCustomRepository.findReservationByIdAndUserIdOrElseThrow(
                        reservationId,
                        new ForeignKey(request.userId())
                )
        );
    }


//    // TODO : 취소는 따로 구현
//    @Transactional
//    public Reservation modifyReservation(Long reservationId, ModifyReservationRequest request) {
//        // 예약 날짜 같이 가져오기.
////        Reservation reservation = findWithReservableItemAndUserByIdAndUserIdOrElseThrow(reservationId, userId);
//        // TODO : 유저와 아이템 정보
////        User user = reservation.getUser();
////        ReservableItem item = reservation.getReservableItem();
//
//        // 변경하려는 값이 같다면 변경 불가.
////        isSameStatus(reservation.getReservationStatus(), request.reservationStatus());
//
//        // 이미 취소, 완료면, 변경 불가.
////        isAlreadyCanceledOrCompleted(reservation);
//
//        // TODO : 날짜가 지났다면 취소 불가.
////        isDatePassedWhenCanceled(reservation, request.reservationRecordStatus());
//
////        reservation
////                .updateStatus(
////                        request.reservationStatus()
////                );
//
//        // TODO : 취소라면 아이템 수량 복구
////        ifCanceledAddReservationItemQuantity(reservation.getReservableItem(), reservation);
//
//        // TODO : 취소라면 유저 잔액 복구
////        ifCanceledAddUserBalance(user, reservation.getQuantity(), item.getPrice());
//
//        return reservation;
//    }
}