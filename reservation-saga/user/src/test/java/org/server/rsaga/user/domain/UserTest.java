package org.server.rsaga.user.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.server.rsaga.common.event.CreateWalletEvent;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("User tests")
@ExtendWith(MockitoExtension.class)
class UserTest {
    String name;
    User user;

    @BeforeEach
    void setUp() {
        name = "testUser";
        user = new User(name);
    }

    @Nested
    @DisplayName("create User tests")
    class CreateUserTests {

        @Test
        @DisplayName("valid name - success")
        void should_createUser_when_validName() {
            // given

            // when
            User user = new User(name);

            // then
            assertEquals(name, user.getName());
        }

        @Test
        @DisplayName("invalid name - throw")
        void should_throw_when_invalidName() {
            // given
            String invalidName = "";

            // when
            IllegalArgumentException aThrows = assertThrows(IllegalArgumentException.class, () -> new User(invalidName));

            // then
            assertEquals("Business name cannot be empty.", aThrows.getMessage());
        }

        @Test
        @DisplayName("name exceeds 20 characters - throw")
        void should_throw_when_nameExceedsMaxLength() {
            // given
            String longName = "thisNameIsWayTooLongToBeValid";

            // when
            IllegalArgumentException aThrows = assertThrows(IllegalArgumentException.class, () -> new User(longName));

            // then
            assertEquals("Business name cannot exceed 20 characters.", aThrows.getMessage());
        }
    }

    @Test
    @DisplayName("changeName() - valid name - success")
    void should_changeName_when_validName() {
        // given
        String newName = "newUserName";

        // when
        user.changeName(newName);

        // then
        assertEquals(newName, user.getName(), "The name should be changed");
    }

    @Test
    @DisplayName("changeName() - invalid name - not changed")
    void should_notChangeName_when_invalidName() {
        // given
        String invalidName = "";

        // when
        user.changeName(invalidName);

        // then
        assertEquals(name, user.getName(), "The name should not be changed.");
    }

    @Test
    @DisplayName("createWallet() - domain event added")
    void should_addCreateWalletEvent_when_createWallet() {
        // when

        // then
        List<Object> domainEvents = user.getDomainEvents();
        assertEquals(1, domainEvents.size());
        assertTrue(domainEvents.get(0) instanceof CreateWalletEvent);
        assertEquals(user.getId(), ((CreateWalletEvent) domainEvents.get(0)).userId());
    }

    @Test
    @DisplayName("clearDomainEvents() - domain events cleared")
    void should_clearDomainEvents_when_clearDomainEvents() {
        // given

        // when
        user.clearDomainEvents();

        // then
        assertTrue(user.getDomainEvents().isEmpty(), "Domain events should be cleared.");
    }
}