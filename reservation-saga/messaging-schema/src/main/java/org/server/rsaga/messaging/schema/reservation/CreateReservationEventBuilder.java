package org.server.rsaga.messaging.schema.reservation;

import jakarta.validation.constraints.NotNull;
import org.server.rsaga.business.VerifyBusinessRequestOuterClass;
import org.server.rsaga.business.VerifyBusinessResponseOuterClass;
import org.server.rsaga.payment.PaymentRequestOuterClass;
import org.server.rsaga.payment.PaymentResponseOuterClass;
import org.server.rsaga.reservableitem.UpdateReservableItemQuantityRequestOuterClass;
import org.server.rsaga.reservableitem.UpdateReservableItemQuantityResponseOuterClass;
import org.server.rsaga.reservableitem.VerifyReservableItemRequestOuterClass;
import org.server.rsaga.reservableitem.VerifyReservableItemResponseOuterClass;
import org.server.rsaga.reservation.*;
import org.server.rsaga.user.VerifyUserRequestOuterClass;
import org.server.rsaga.user.VerifyUserResponseOuterClass;

public class CreateReservationEventBuilder {
    private final CreateReservationEvent.Builder builder = CreateReservationEvent.newBuilder();

    public static CreateReservationEventBuilder builder() {
        return new CreateReservationEventBuilder();
    }

    // init event
    public CreateReservationEventBuilder setRegisterReservationInitRequest(
            String paymentType,
            long userId,
            long businessId,
            long reservableItemId,
            long reservableTimeId,
            long requestQuantity
    ) {
        CreateReservationInitRequestOuterClass.CreateReservationInitRequest createReservationInitRequest = CreateReservationInitRequestOuterClass.CreateReservationInitRequest
                .newBuilder()
                .setPaymentType(paymentType)
                .setUserId(userId)
                .setBusinessId(businessId)
                .setReservableItemId(reservableItemId)
                .setReservableItemTimeId(reservableTimeId)
                .setRequestQuantity(requestQuantity)
                .build();

        builder
                .setCreateReservationInit(createReservationInitRequest);
        return this;
    }

    // business 검증
    public CreateReservationEventBuilder setVerifyBusinessRequest(long businessId) {
        VerifyBusinessRequestOuterClass.VerifyBusinessRequest verifyBusinessRequest = VerifyBusinessRequestOuterClass.VerifyBusinessRequest.newBuilder()
                .setBusinessId(businessId)
                .build();
        builder
                .setVerifyBusiness(verifyBusinessRequest);
        return this;
    }
    public CreateReservationEventBuilder setVerifyBusinessResponse(long businessId) {
        VerifyBusinessResponseOuterClass.VerifyBusinessResponse verifyBusinessResponse = VerifyBusinessResponseOuterClass.VerifyBusinessResponse.newBuilder()
                .setBusinessId(businessId)
                .build();
        builder
                .setVerifiedBusiness(verifyBusinessResponse);
        return this;
    }

    // item 검증
    public CreateReservationEventBuilder setVerifyReservableItemRequest(long reservableItemId,
                                                                        long reservableTimeId,
                                                                        long businessId,
                                                                        long requestQuantity) {
        VerifyReservableItemRequestOuterClass.VerifyReservableItemRequest verifyReservableItemRequest = VerifyReservableItemRequestOuterClass.VerifyReservableItemRequest.newBuilder()
                .setReservableItemId(reservableItemId)
                .setReservableTimeId(reservableTimeId)
                .setBusinessId(businessId)
                .setRequestQuantity(requestQuantity)
                .build();
        builder
                .setVerifyReservableItem(verifyReservableItemRequest);
        return this;
    }

    public CreateReservationEventBuilder setVerifyReservableItemResponse(
            long reservableItemId,
            long reservableTimeId,
            long reservableItemPrice,
            long maxQuantityPerUser
    ) {
        VerifyReservableItemResponseOuterClass.VerifyReservableItemResponse verifyReservableItemResponse = VerifyReservableItemResponseOuterClass.VerifyReservableItemResponse.newBuilder()
                .setReservableItemId(reservableItemId)
                .setReservableTimeId(reservableTimeId)
                .setReservableItemPrice(reservableItemPrice)
                .setMaxQuantityPerUser(maxQuantityPerUser)
                .build();
        builder
                .setVerifiedReservableItem(verifyReservableItemResponse);
        return this;
    }

    // user 검증
    public CreateReservationEventBuilder setVerifyUserRequest(long userId) {
        VerifyUserRequestOuterClass.VerifyUserRequest verifyUserRequest = VerifyUserRequestOuterClass.VerifyUserRequest.newBuilder()
                .setUserId(userId)
                .build();
        builder
                .setVerifyUser(verifyUserRequest);
        return this;
    }

    public CreateReservationEventBuilder setVerifyUserResponse(long userId) {
        VerifyUserResponseOuterClass.VerifyUserResponse verifyUserResponse = VerifyUserResponseOuterClass.VerifyUserResponse.newBuilder()
                .setUserId(userId)
                .build();
        builder
                .setVerifiedUser(verifyUserResponse);
        return this;
    }

    // 인당 구매제한 검사, Reservation 등록
    public CreateReservationEventBuilder setCheckReservationLimitRequest(
            @NotNull long userId,
            long reservableItemId,
            long reservableTimeId,
            long businessId,
            long reservationId,
            long maxQuantityPerUser,
            long requestQuantity,
            ReservationStatusOuterClass.ReservationStatus reservationStatus
    ) {
        CheckReservationLimitRequestOuterClass.CheckReservationLimitRequest checkReservationLimitRequest = CheckReservationLimitRequestOuterClass.CheckReservationLimitRequest.newBuilder()
                .setReservableItemId(reservableItemId)
                .setReservableTimeId(reservableTimeId)
                .setUserId(userId)
                .setBusinessId(businessId)
                .setReservationId(
                        reservationId
                )
                .setMaxQuantityPerUser(maxQuantityPerUser)
                .setRequestQuantity(requestQuantity)
                .setStatus(reservationStatus)
                .build();

        builder
                .setCheckReservation(checkReservationLimitRequest);
        return this;
    }
    public CreateReservationEventBuilder setCheckReservationLimitResponse(
            long reservationId
    ) {
        CheckReservationLimitResponseOuterClass.CheckReservationLimitResponse checkReservationLimitResponse = CheckReservationLimitResponseOuterClass.CheckReservationLimitResponse.newBuilder()
                .setReservationId(
                        reservationId
                )
                .build();
        builder
                .setCheckedReservation(checkReservationLimitResponse);
        return this;
    }

    // item quantity 감소
    public CreateReservationEventBuilder setUpdateReservableItemQuantityRequest(
            long reservableItemId,
            long reservableTimeId,
            long requestQuantity) {
        UpdateReservableItemQuantityRequestOuterClass.UpdateReservableItemQuantityRequest updateReservableItemQuantityRequest = UpdateReservableItemQuantityRequestOuterClass.UpdateReservableItemQuantityRequest.newBuilder()
                .setReservableItemId(reservableItemId)
                .setReservableTimeId(reservableTimeId)
                .setRequestQuantity(requestQuantity)
                .build();
        builder
                .setUpdateReservableItemQuantity(updateReservableItemQuantityRequest);
        return this;
    }

    public CreateReservationEventBuilder setUpdateReservableItemQuantityResponse(
            long reservableItemId
    ) {
        UpdateReservableItemQuantityResponseOuterClass.UpdateReservableItemQuantityResponse updateReservableItemQuantityResponse = UpdateReservableItemQuantityResponseOuterClass.UpdateReservableItemQuantityResponse.newBuilder()
                .setReservableItemId(reservableItemId)
                .build();

        builder
                .setUpdatedReservableItemQuantity(updateReservableItemQuantityResponse);
        return this;
    }

    public CreateReservationEventBuilder setPaymentRequest(
            String paymentType,
            long userId,
            long reservationId,
            long amount
    ) {
        PaymentRequestOuterClass.PaymentRequest paymentRequest = PaymentRequestOuterClass.PaymentRequest.newBuilder()
                .setPaymentType(paymentType)
                .setUserId(userId)
                .setReservationId(reservationId)
                .setAmount(amount)
                .build();

        builder
                .setPay(paymentRequest);
        return this;
    }

    public CreateReservationEventBuilder setPaymentResponse(
            long userId
    ) {
        PaymentResponseOuterClass.PaymentResponse paymentResponse = PaymentResponseOuterClass.PaymentResponse.newBuilder()
                .setUserId(userId)
                .build();

        builder
                .setPaid(paymentResponse);
        return this;
    }

    // final event
    public CreateReservationEventBuilder setRegisterReservationFinalRequest(
            long reservationId
    ) {
        CreateReservationFinalRequestOuterClass.CreateReservationFinalRequest createReservationFinalRequest = CreateReservationFinalRequestOuterClass.CreateReservationFinalRequest.newBuilder()
                .setReservationId(
                        reservationId
                )
                .build();
        builder
                .setCreateReservationFinal(createReservationFinalRequest);
        return this;
    }

    public CreateReservationEventBuilder setRegisterReservationFinalResponse(
            long reservationId,
            Long businessId,
            Long userId,
            Long reservableItemId,
            Long reservableTimeId,
            Long quantity
    ) {
        CreateReservationFinalResponseOuterClass.CreateReservationFinalResponse createReservationFinalResponse = CreateReservationFinalResponseOuterClass.CreateReservationFinalResponse.newBuilder()
                .setReservationId(reservationId)
                .setBusinessId(businessId)
                .setUserId(userId)
                .setReservableItemId(reservableItemId)
                .setReservableTimeId(reservableTimeId)
                .setQuantity(quantity)
                .build();
        builder
                .setCreatedReservationFinal(createReservationFinalResponse);
        return this;
    }

    public CreateReservationEvent build() {
        return builder
                .build();
    }
}
