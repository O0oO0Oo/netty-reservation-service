package org.server.rsaga.user.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.server.rsaga.common.domain.BaseTime;
import org.server.rsaga.common.domain.Money;


@Entity
@Getter
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private Money balance;

    @Embedded
    private BaseTime baseTime;

    public User(String name, Money balance) {
        checkName(name);
        this.name = name;
        this.balance = balance;
    }

    /**
     * ---------------------- getter ----------------------
     */
    public long getBalance() {
        return this.balance.getAmount();
    }

    /**
     * ---------------------- setter ----------------------
     */

    public void changeName(String newName) {
        if (newName != null && !newName.trim().isEmpty()) {
            this.name = newName;
        }
    }

    public void addBalance(Money newMoney) {
        if(newMoney != null) {
            this.balance = this.balance.add(newMoney);
        }
    }

    public void subtractBalance(Money newMoney) {
        if (newMoney != null) {
            this.balance = this.balance.subtract(newMoney);
        }
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