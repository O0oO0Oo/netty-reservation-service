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
@Table(name = "wallet", indexes = {
        @Index(name = "idx_user_id", columnList = "user_id")
})
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

    public Wallet(
            final ForeignKey userId,
            final Money balance) {
        checkNull(userId);
        this.userId = userId;

        checkNull(balance);
        this.balance = balance;
    }

    /**
     * ---------------------- getter ----------------------
     */
    public long getUserId() {
        return userId.getId();
    }

    public Money getBalance() {
        return balance;
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

    /**
     * ---------------------- validation ----------------------
     */
    private void checkNull(Object object) {
        if (object == null) {
            throw new IllegalArgumentException("The parameter should not be null.");
        }
    }
}