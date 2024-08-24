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

    /**
     * todo : 예약 수정 구현
     * <pre>
     * public ReservationDetailsResponse modifyReservation(Long reservationId, ModifyReservationRequest request)
     * 1. 날짜가 지났다면 취소 불가.
     * 2. 이미 취소, 완료상태라면 취소 불가.
     * 2. 취소라면 아이템 수량, 유저 지갑 잔액 복구
     * </pre>
     */
}