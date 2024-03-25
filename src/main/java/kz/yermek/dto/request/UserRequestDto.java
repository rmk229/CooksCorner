package kz.yermek.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record UserRequestDto(
        @NotNull(message = "Email field should not be null")
        @NotBlank(message = "Email field should not be blank")
        @Pattern(regexp = "^[a-zA-Z0-9._-]+@[a-zA-Z0-9. -]+\\.[a-zA-Z]{2,4}$", message = "Email should be valid")
        String email,
        String name,
        @NotNull(message = "Password field should not be null")
        @NotBlank(message = "Password is required")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!])(?!.*\\s).{8,15}$", message = "Password should be valid")
        String password,

        String confirmPassword) {
}
