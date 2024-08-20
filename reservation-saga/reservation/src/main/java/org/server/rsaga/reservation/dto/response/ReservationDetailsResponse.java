package org.server.rsaga.reservation.dto.response;

import org.server.rsaga.reservation.CreateReservationFinalResponseOuterClass;
import org.server.rsaga.reservation.domain.Reservation;

import java.util.ArrayList;
import java.util.List;

public record ReservationDetailsResponse(
        long reservationId,
        long businessId,
        long userId,
        long reservableItemId,
        long reservableTimeId,
        long quantity
) {
    public static ReservationDetailsResponse of(CreateReservationFinalResponseOuterClass.CreateReservationFinalResponse event) {
        return new ReservationDetailsResponse(
                event.getReservationId(),
                event.getBusinessId(),
                event.getUserId(),
                event.getReservableItemId(),
                event.getReservableTimeId(),
                event.getQuantity()
        );
    }

    public static ReservationDetailsResponse of(Reservation reservation) {
        return new ReservationDetailsResponse(
                reservation.getId(),
                reservation.getBusinessId(),
                reservation.getUserId(),
                reservation.getReservableItemId(),
                reservation.getReservableTimeId(),
                reservation.getQuantity()
        );
    }

    public static List<ReservationDetailsResponse> of(List<Reservation> reservationList) {
        List<ReservationDetailsResponse> responses = new ArrayList<>(reservationList.size() + 1);
        for (Reservation reservation : reservationList) {
            responses.add(
                    of(reservation)
            );
        }
        return responses;
    }
}
