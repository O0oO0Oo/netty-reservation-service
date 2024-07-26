package org.server.reservation.business.service;

import org.server.reservation.core.common.exception.CustomException;
import org.server.reservation.core.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.server.reservation.business.dto.ModifyBusinessRequest;
import org.server.reservation.business.dto.RegisterBusinessRequest;
import org.server.reservation.business.entity.Business;
import org.server.reservation.business.repository.BusinessRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class BusinessService {
    private final BusinessRepository businessRepository;

    @Transactional(readOnly = true)
    public Business findBusiness(Long businessId) {
        return findBusinessOrElseThrow(businessId);
    }

    private Business findBusinessOrElseThrow(Long businessId) {
        return businessRepository.findById(businessId)
                .orElseThrow(
                        () -> new CustomException(ErrorCode.BUSINESS_NOT_FOUND)
                );
    }

    @Transactional
    public Business registerBusiness(RegisterBusinessRequest request) {
        Business business = Business.builder()
                .name(request.name())
                .businessType(request.businessType())
                .build();

        return businessRepository.save(business);
    }

    @Transactional
    public Business modifyBusiness(Long businessId, ModifyBusinessRequest request) {
        Business business = findBusinessOrElseThrow(businessId);

        business.setName(
                Objects.requireNonNullElse(request.name(), business.getName())
        );
        business.setBusinessType(
                Objects.requireNonNullElse(request.businessType(), business.getBusinessType())
        );

        return business;
    }

    @Transactional
    public void deleteBusiness(Long businessId) {
        businessRepository.deleteById(businessId);
    }
}