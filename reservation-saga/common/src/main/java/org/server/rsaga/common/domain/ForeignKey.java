package org.server.rsaga.common.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Value;

@Value
@Embeddable
public class ForeignKey {
    @Column(name = "foreign_key", nullable = false)
    Long id;

    public ForeignKey(final Long id) {
        checkNull(id);
        this.id = id;
    }

    private void checkNull(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("The id should not be null.");
        }
    }

    protected ForeignKey() {
        this.id = null;
    }
}
