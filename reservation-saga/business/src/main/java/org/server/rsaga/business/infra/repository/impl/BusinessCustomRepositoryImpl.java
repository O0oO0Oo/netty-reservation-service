package org.server.rsaga.business.infra.repository.impl;

import lombok.RequiredArgsConstructor;
import org.server.rsaga.business.domain.Business;
import org.server.rsaga.business.infra.repository.BusinessJpaRepository;
import org.server.rsaga.business.infra.repository.BusinessCustomRepository;
import org.server.rsaga.common.exception.CustomException;
import org.server.rsaga.common.exception.ErrorCode;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BusinessCustomRepositoryImpl implements BusinessCustomRepository {
    private final BusinessJpaRepository businessJpaRepository;

    @Override
    public Business findByIdOrElseThrow(Long id) {
        return businessJpaRepository.findById(id)
                .orElseThrow(
                        () -> new CustomException(ErrorCode.BUSINESS_NOT_FOUND)
                );
    }

    @Override
    public Business findByIdAndNotClosedOrElseThrow(Long id) {
        return businessJpaRepository.findByIdAndClosedFalse(id)
                .orElseThrow(
                        () -> new CustomException(ErrorCode.BUSINESS_NOT_FOUND)
                );
    }
}