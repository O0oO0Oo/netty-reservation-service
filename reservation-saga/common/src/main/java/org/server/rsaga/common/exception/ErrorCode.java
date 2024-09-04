package org.server.rsaga.common.exception;

import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.Getter;

@Getter
public enum  ErrorCode {
    // Common
    BAD_REQUEST_BODY(HttpResponseStatus.BAD_REQUEST, "C001", "잘못된 요청입니다. 다음을 확인하세요"),
    BAD_REQUEST_PATH_VARIABLE(HttpResponseStatus.BAD_REQUEST, "C002", "잘못된 요청입니다. Path Parameter 를 확인하세요"),
    INVALID_JSON(HttpResponseStatus.BAD_REQUEST, "C003", "잘못된 요청입니다. Json 형식을 확인하세요."),
    

    // Business
    BUSINESS_NOT_FOUND(HttpResponseStatus.NOT_FOUND, "B001", "회사를 찾을 수 없습니다."),


    // User
    USER_NOT_FOUND(HttpResponseStatus.NOT_FOUND, "U001", "유저를 찾을 수 없습니다."),
    USER_NAME_ALREADY_EXIST(HttpResponseStatus.BAD_REQUEST, "U002", "이미 존재하는 이름 입니다."),


    // Reservable Item
    RESERVABLE_ITEM_NOT_FOUND(HttpResponseStatus.NOT_FOUND, "I001", "상품을 찾을 수 없습니다."),
    RESERVATION_DATE_LATER_TODAY(HttpResponseStatus.BAD_REQUEST, "I002", "예약날은 현재 날짜 이후여야 합니다."),
    RESERVABLE_ITEM_IS_NOT_AVAILABLE(HttpResponseStatus.BAD_REQUEST, "I003", "종료된 상품 입니다."),
    REGISTER_RESERVABLE_ITEM_FAILED(HttpResponseStatus.INTERNAL_SERVER_ERROR, "I004", "상품 등록에 실패했습니다."),
    EXCEED_PURCHASE_LIMIT(HttpResponseStatus.BAD_REQUEST, "I005", "인당 구매제한이상으로 구매 불가능 합니다."),

    // Reservable Time
    RESERVABLE_TIME_NOT_FOUND(HttpResponseStatus.NOT_FOUND, "T001", "예약 가능 시간을 찾을 수 없습니다."),
    INSUFFICIENT_STOCK(HttpResponseStatus.BAD_REQUEST, "T002","재고가 부족합니다."),

    // Reservation
    RESERVATION_NOT_FOUND(HttpResponseStatus.NOT_FOUND, "R001", "기록을 찾을 수 없습니다."),
    QUANTITY_INSUFFICIENT(HttpResponseStatus.BAD_REQUEST, "R002", "남은 수량이 부족합니다."),
    ALREADY_CANCELED_OR_COMPLETED(HttpResponseStatus.BAD_REQUEST, "R003", "이미 취소된 예약 입니다."),
    RESERVATION_DATE_PASSED(HttpResponseStatus.BAD_REQUEST, "R004", "날짜가 지난 상품은 예약이나 상태 변경이 불가능 합니다."),
    SAME_STATUS(HttpResponseStatus.BAD_REQUEST, "R005", "변경하려는 값이 같습니다."),
    MAX_QUANTITY_EXCEEDED(HttpResponseStatus.BAD_REQUEST, "R006", "최대 구매 수량을 초과했습니다."),
    INVALID_RESERVATION_STATUS(HttpResponseStatus.BAD_REQUEST, "R007", "처리 할 수 없는 예약 상태 입니다."),

    // wallet
    WALLET_NOT_FOUND(HttpResponseStatus.NOT_FOUND, "W001", "지갑을 찾을 수 없습니다."),
    INSUFFICIENT_BALANCE(HttpResponseStatus.BAD_REQUEST, "W002", "잔액이 부족합니다."),

    // payment
    PAYMENT_NOT_FOUND(HttpResponseStatus.NOT_FOUND, "P001", "결제 기록을 찾을 수 없습니다."),
    PAYMENT_TYPE_NOT_FOUND(HttpResponseStatus.NOT_FOUND, "P002", "결제 타입을 찾을 수 없습니다."),

    // saga
    PROCESSING_ERROR(HttpResponseStatus.BAD_REQUEST, "S001", "처리 중 에러가 발생했습니다.");

    private final HttpResponseStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpResponseStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
