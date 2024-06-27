package org.server.reservation.user.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Getter
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false)
    @JsonProperty("userId")
    private Long id;

    @Setter
    @Column(nullable = false, unique = true)
    private String name;

    @Setter
    @Min(value = 0, message = "잔고는 0 이상이어야 합니다.")
    @Column(nullable = false)
    private Long balance;

    @Builder
    public User(String name, Long balance) {
        this.name = name;
        this.balance = balance;
    }
}
