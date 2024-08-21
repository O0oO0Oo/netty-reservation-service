package org.server.rsaga.user.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.server.rsaga.common.domain.BaseTime;
import org.server.rsaga.common.event.CreateWalletEvent;
import org.server.rsaga.common.event.EventPublisher;


@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Embedded
    private BaseTime baseTime;

    public User(String name) {
        checkName(name);
        this.name = name;
    }

    /**
     * ---------------------- setter ----------------------
     */

    public void changeName(String newName) {
        if (newName != null && !newName.trim().isEmpty()) {
            this.name = newName;
        }
    }

    public void createWallet() {
        EventPublisher.publish(
                new CreateWalletEvent(this.id)
        );
    }

    /**
     * ---------------------- validation ----------------------
     */
    private void checkName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Business name cannot be empty.");
        }

        if (name.length() > 20) {
            throw new IllegalArgumentException("Business name cannot exceed 20 characters.");
        }
    }
}