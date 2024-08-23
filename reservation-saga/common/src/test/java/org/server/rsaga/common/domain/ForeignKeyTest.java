package org.server.rsaga.common.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ForeignKey value tests")
class ForeignKeyTest {

    @Test
    @DisplayName("create ForeignKey - null id - failure")
    void should_throw_when_createNullId() {
        // given

        // when
        IllegalArgumentException aThrows = assertThrows(
                IllegalArgumentException.class, () -> new ForeignKey(null)
        );

        // then
        assertEquals(aThrows.getMessage(), "The id should not be null.");
    }

    @Test
    @DisplayName("create ForeignKey - valid parameter - success")
    void should_success_when_create() {
        // given

        // when
        ForeignKey foreignKey = new ForeignKey(1L);

        // then
        assertEquals(1L, foreignKey.getId(), "The id should be 1.");
    }

}