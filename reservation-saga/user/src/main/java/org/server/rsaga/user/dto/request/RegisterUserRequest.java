package org.server.rsaga.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

public record RegisterUserRequest(
        @NotBlank(message = "이름은 필수 입력 값이며 공백이면 안됩니다")
        @Length(min = 6, max = 20, message = "이름은 6 ~ 20자 입니다.")
        String name
) {
}
