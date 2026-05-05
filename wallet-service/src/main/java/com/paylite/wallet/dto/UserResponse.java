package com.paylite.wallet.dto;

import com.paylite.wallet.entity.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * The shape of the JSON we return after successful signup (and login, later).
 *
 * Deliberately omits passwordHash — never expose it via API.
 * Includes id and createdAt — useful for the client.
 *
 * Provides a static factory method `from(User)` so service code can convert
 * an entity to a response in one line: `UserResponse.from(savedUser)`.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private Long id;
    private String email;
    private String fullName;
    private String phone;
    private LocalDateTime createdAt;

    /**
     * Static factory: convert a User entity to a UserResponse DTO.
     * Centralizing the mapping here means we only define "what's safe to expose"
     * in one place.
     */
    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .createdAt(user.getCreatedAt())
                .build();
    }
}