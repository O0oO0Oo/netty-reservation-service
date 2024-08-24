package org.server.rsaga.common.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ForeignKey value tests")
class ForeignKeyTest {

    @Test
    @DisplayName("create ForeignKey - null id - throw")
    void should_throw_when_createNullId() {
        // given

        // when
        IllegalArgumentException aThrows = assertThrows(
                IllegalArgumentException.class, () -> new ForeignKey(null)
        );

        // then
        assertEquals("The id should not be null.", aThrows.getMessage());
    }

    @Test
    @DisplayName("create ForeignKey - valid parameter - create ForeignKey")
    void should_created_when_create() {
        // given

        // when
        ForeignKey foreignKey = new ForeignKey(1L);

        // then
        assertEquals(1L, foreignKey.getId(), "The id should be 1.");
    }

}