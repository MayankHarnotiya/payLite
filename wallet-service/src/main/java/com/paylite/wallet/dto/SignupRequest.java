package com.paylite.wallet.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The shape of the JSON body for POST /api/auth/signup.
 *
 * Validation annotations (@NotBlank, @Email, @Size, @Pattern) are checked
 * automatically by Spring when the controller method is annotated with @Valid.
 * Invalid input returns HTTP 400 Bad Request with details — we don't write
 * the validation logic ourselves.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignupRequest {

    /**
     * Email is required (not blank), in a valid email format, and ≤100 chars
     * to match the DB column. The DB also enforces uniqueness — we'll catch that
     * collision in the service layer with a friendlier error message.
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Email format is invalid")
    @Size(max = 100, message = "Email cannot exceed 100 characters")
    private String email;

    /**
     * Plain-text password from the user. We hash it before storing.
     * Min 8 chars is the modern OWASP recommendation; max 72 because BCrypt
     * truncates beyond 72 bytes silently — accepting longer would create a
     * security gap where extra characters effectively don't matter.
     */
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 72, message = "Password must be between 8 and 72 characters")
    private String password;

    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Full name cannot exceed 100 characters")
    private String fullName;

    /**
     * Optional. If provided, must be a valid 10-digit Indian mobile number
     * (starting with 6/7/8/9). Null is allowed.
     */
    @Pattern(
            regexp = "^[6-9]\\d{9}$",
            message = "Phone must be a valid 10-digit Indian mobile number"
    )
    private String phone;
}