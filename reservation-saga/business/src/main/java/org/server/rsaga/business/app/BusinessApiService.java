package org.server.rsaga.business.app;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.server.rsaga.business.domain.Business;
import org.server.rsaga.business.domain.BusinessCategory;
import org.server.rsaga.business.dto.request.ModifyBusinessRequest;
import org.server.rsaga.business.dto.request.RegisterBusinessRequest;
import org.server.rsaga.business.dto.response.BusinessDetailsResponse;
import org.server.rsaga.business.infra.repository.BusinessCustomRepository;
import org.server.rsaga.business.infra.repository.BusinessJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BusinessApiService {
    private final BusinessCustomRepository businessCustomRepository;
    private final BusinessJpaRepository businessJpaRepository;

    @Transactional(readOnly = true)
    public BusinessDetailsResponse findBusiness(Long businessId) {
        return BusinessDetailsResponse.of(
                businessCustomRepository.findByIdOrElseThrow(businessId)
        );
    }

    @Transactional
    public BusinessDetailsResponse registerBusiness(RegisterBusinessRequest request) {
        Business business = new Business(
                request.name(),
                new BusinessCategory(
                        request.businessMajorCategory(),
                        request.businessSubCategory(),
                        request.businessDetailCategory()
                )
        );

        return BusinessDetailsResponse.of(businessJpaRepository.save(business));
    }

    @Transactional
    public BusinessDetailsResponse modifyBusiness(Long businessId, ModifyBusinessRequest request) {
        Business business = businessCustomRepository.findByIdOrElseThrow(businessId);

        BusinessCategory businessCategory = null;
        if (request.businessMajorCategory() != null) {
            businessCategory = new BusinessCategory(
                    request.businessMajorCategory(),
                    request.businessSubCategory(),
                    request.businessDetailCategory()
            );
        }

        business.changeName(
                        request.name()
                )
                .changeBusinessCategory(
                        businessCategory
                );

        return BusinessDetailsResponse.of(business);
    }

    // todo : 관련 item 도 모두 사용불가능하게 해야함.
    @Transactional
    public void deleteBusiness(Long businessId) {
        Business business = businessCustomRepository.findByIdOrElseThrow(businessId);
        business.closeBusiness();
    }
}