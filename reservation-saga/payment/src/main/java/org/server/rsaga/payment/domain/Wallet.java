package org.server.rsaga.payment.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.server.rsaga.common.domain.ForeignKey;
import org.server.rsaga.common.domain.Money;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Wallet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wallet_id", nullable = false)
    private Long id;

    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "user_id", nullable = false))
    private ForeignKey userId;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "balance", nullable = false))
    private Money balance;

    public Wallet(Long userId, Money balance) {
        this.userId = new ForeignKey(userId);
        this.balance = balance;
    }

    /**
     * ---------------------- getter ----------------------
     */
    public long getBalance() {
        return balance.getAmount();
    }

    public long getUserId() {
        return userId.getId();
    }

    /**
     * ---------------------- setter ----------------------
     */
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
}