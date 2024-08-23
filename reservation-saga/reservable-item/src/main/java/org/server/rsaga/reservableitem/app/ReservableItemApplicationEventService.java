package org.server.rsaga.reservableitem.app;

import lombok.RequiredArgsConstructor;
import org.server.rsaga.common.domain.ForeignKey;
import org.server.rsaga.common.event.BusinessClosedEvent;
import org.server.rsaga.reservableitem.domain.ReservableItem;
import org.server.rsaga.reservableitem.infra.repository.ReservableItemJpaRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservableItemApplicationEventService {
    private final ReservableItemJpaRepository reservableItemJpaRepository;

    @EventListener
    @Transactional
    public void handleBusinessClosedEvent(BusinessClosedEvent event) {
        ForeignKey businessId = new ForeignKey(
                event.businessId()
        );
        List<ReservableItem> reservableItems = reservableItemJpaRepository.findByBusinessIdWithTimes(businessId);

        for (ReservableItem reservableItem : reservableItems) {
            reservableItem.changeIsItemAvailable(Boolean.FALSE);
        }
    }
}
