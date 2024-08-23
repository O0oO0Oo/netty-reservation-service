package org.server.rsaga.business.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.server.rsaga.business.domain.constant.BusinessDetailCategory;
import org.server.rsaga.business.domain.constant.BusinessMajorCategory;
import org.server.rsaga.business.domain.constant.BusinessSubCategory;
import org.server.rsaga.common.domain.BaseTime;

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

    public void closeBusiness() {
        this.closed = true;
        
        // todo 관련 item 모두 사용불가능하게
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
}
