package org.server.rsaga.business.app;

import lombok.RequiredArgsConstructor;
import org.server.rsaga.business.infra.repository.BusinessJpaRepository;
import org.server.rsaga.common.event.BusinessValidationEvent;
import org.server.rsaga.common.exception.CustomException;
import org.server.rsaga.common.exception.ErrorCode;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class BusinessApplicationEventService {
    private final BusinessJpaRepository businessJpaRepository;

    @EventListener
    @Transactional(readOnly = true)
    public void handleBusinessValidationRequest(BusinessValidationEvent event) {
        boolean isValid = businessJpaRepository.existsByIdAndClosedFalse(event.getBusinessId());
        if(!isValid){
            throw new CustomException(ErrorCode.BUSINESS_NOT_FOUND);
        }
    }
}