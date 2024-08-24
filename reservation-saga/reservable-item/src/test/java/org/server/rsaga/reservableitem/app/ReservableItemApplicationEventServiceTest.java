package org.server.rsaga.reservableitem.app;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.server.rsaga.common.domain.ForeignKey;
import org.server.rsaga.common.event.BusinessClosedEvent;
import org.server.rsaga.reservableitem.domain.ReservableItem;
import org.server.rsaga.reservableitem.infra.repository.ReservableItemJpaRepository;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("ReservableItemApplicationEventService tests")
@ExtendWith(MockitoExtension.class)
class ReservableItemApplicationEventServiceTest {

    @Mock
    ReservableItemJpaRepository reservableItemJpaRepository;

    @InjectMocks
    ReservableItemApplicationEventService reservableItemApplicationEventService;


    @Test
    @DisplayName("handleBusinessClosedEvent() - valid event - succeed")
    void should_succeed_when_handleBusinessClosedEvent() {
        // given
        Long businessIdValue = 1L;
        ForeignKey businessId = new ForeignKey(businessIdValue);
        BusinessClosedEvent event = new BusinessClosedEvent(businessIdValue);

        ReservableItem reservableItem1 = mock(ReservableItem.class);
        ReservableItem reservableItem2 = mock(ReservableItem.class);
        List<ReservableItem> reservableItems = List.of(reservableItem1, reservableItem2);

        when(reservableItemJpaRepository.findByBusinessIdWithTimes(businessId)).thenReturn(reservableItems);

        // when
        reservableItemApplicationEventService.handleBusinessClosedEvent(event);

        // then
        verify(reservableItemJpaRepository).findByBusinessIdWithTimes(businessId);
        verify(reservableItem1).changeIsItemAvailable(Boolean.FALSE);
        verify(reservableItem2).changeIsItemAvailable(Boolean.FALSE);
    }
}