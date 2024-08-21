package org.server.rsaga.common.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Value;

@Value
@Embeddable
public class ForeignKey {
    @Column(name = "foreign_key", nullable = false)
    Long id;

    public ForeignKey(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("The id should not be null.");
        }
        this.id = id;
    }

    protected ForeignKey() {
        this.id = null;
    }
}
