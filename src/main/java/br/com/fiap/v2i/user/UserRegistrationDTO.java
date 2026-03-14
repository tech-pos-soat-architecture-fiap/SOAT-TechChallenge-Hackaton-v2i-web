package br.com.fiap.v2i.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRegistrationDTO(
        @NotBlank(message = "Username is required")
        String username,

        @NotBlank(message = "Password is required")
        String password,

        @Email(message = "Email should be valid")
        @NotBlank(message = "Email is required")
        String email
) {}
