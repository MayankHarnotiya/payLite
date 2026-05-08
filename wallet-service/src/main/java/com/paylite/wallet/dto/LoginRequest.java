package com.paylite.wallet.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Input shape for POST /api/auth/login.
 *
 * Note: we keep validations LOOSE here on purpose.
 * - No @Size(min=8) on password — we don't want to leak our password rules
 *   to attackers via 400 errors.
 * - We just check both fields are present and email is reasonably formed.
 * - Wrong credentials → InvalidCredentialsException → 401, not validation 400.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email format is invalid")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;
}