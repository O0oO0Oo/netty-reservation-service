package org.server.rsaga.business.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.server.rsaga.business.domain.constant.BusinessDetailCategory;
import org.server.rsaga.business.domain.constant.BusinessMajorCategory;
import org.server.rsaga.business.domain.constant.BusinessSubCategory;
import org.server.rsaga.common.domain.BaseTime;
import org.server.rsaga.common.event.BusinessClosedEvent;
import org.springframework.data.domain.AfterDomainEventPublication;
import org.springframework.data.domain.DomainEvents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Business {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "business_id", nullable = false)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Embedded
    @Column(nullable = false)
    private BusinessCategory businessCategory;

    @Column(nullable = false)
    private boolean closed;

    @Embedded
    private BaseTime baseTime;

    @Transient
    private final List<Object> domainEvents = new ArrayList<>();

    public Business(final String name,final BusinessCategory businessCategory) {
        checkName(name);
        this.name = name;

        checkBusinessCategory(businessCategory);
        this.businessCategory = businessCategory;

        this.closed = false;
    }

    /**
     * ---------------------- getter ----------------------
     */
    public BusinessMajorCategory getMajorCategory() {
        return this.businessCategory.getMajorCategory();
    }

    public BusinessSubCategory getSubCategory() {
        return this.businessCategory.getSubCategory();
    }

    public BusinessDetailCategory getDetailCategory() {
        return this.businessCategory.getDetailCategory();
    }

    /**
     * ---------------------- setter ----------------------
     */
    public Business changeName(final String newName) {
        if (newName != null && !newName.trim().isEmpty()) {
            this.name = newName;
        }
        return this;
    }

    public Business changeBusinessCategory(final BusinessCategory newBusinessCategory) {
        if(newBusinessCategory != null) {
            this.businessCategory = newBusinessCategory;
        }
        return this;
    }

    /**
     * 회사 폐업, 회사에서 등록된 아이템들은 모두 이용 불가가 되어야한다.
     */
    public void closeBusiness() {
        if(!closed) {
            this.closed = true;
            addDomainEvent(new BusinessClosedEvent(id));
        }
    }

    /**
     * ---------------------- validation ----------------------
     */

    private void checkName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Business name cannot be empty.");
        }

        if (name.length() > 50) {
            throw new IllegalArgumentException("Business name cannot exceed 50 characters.");
        }
    }

    private void checkBusinessCategory(BusinessCategory businessCategory) {
        if (businessCategory == null) {
            throw new IllegalArgumentException("Business category cannot be null.");
        }
    }

    /**
     * ---------------------- event ----------------------
     */

    private void addDomainEvent(Object event) {
        this.domainEvents.add(event);
    }

    @DomainEvents
    public List<Object> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    @AfterDomainEventPublication
    public void clearDomainEvents() {
        this.domainEvents.clear();
    }
}