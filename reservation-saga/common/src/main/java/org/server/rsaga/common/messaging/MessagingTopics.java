package org.server.rsaga.common.messaging;

public enum MessagingTopics {
    // Create Reservation
    CREATE_RESERVATION_VERIFY_USER("create-reservation-verify-user"),
    CREATE_RESERVATION_VERIFY_BUSINESS("create-reservation-verify-business"),
    CREATE_RESERVATION_VERIFY_RESERVABLEITEM("create-reservation-verify-reservableitem"),
    CREATE_RESERVATION_CHECK_RESERVATION_LIMIT("create-reservation-check-reservation-limit"),
    CREATE_RESERVATION_UPDATE_RESERVABLEITEM_QUANTITY("create-reservation-update-reservableitem-quantity"),
    CREATE_RESERVATION_PAYMENT("create-reservation-payment"),
    CREATE_RESERVATION_FINAL_STEP("create-reservation-final-step"),
    CREATE_RESERVATION_RESPONSE("create-reservation-response"),


    // Modify Reservation

    // Register ReservableItem
    ;

    MessagingTopics(String name) {
    }
}