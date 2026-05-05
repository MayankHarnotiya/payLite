package com.paylite.wallet.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Represents a PayLite user — anyone who can log in to the platform.
 * Maps 1:1 to the `users` table created by Flyway migration V1.
 *
 * Lombok annotations generate boilerplate at compile time:
 *   @Getter / @Setter        - getters and setters for every field
 *   @NoArgsConstructor       - empty constructor (required by JPA)
 *   @AllArgsConstructor      - constructor with all fields (handy for testing)
 *   @Builder                 - fluent builder pattern: User.builder().email("...").build()
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    /**
     * Primary key. The database (MySQL AUTO_INCREMENT) generates this on INSERT,
     * and Hibernate reads it back into the entity.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    /**
     * BCrypt hash of the password. NEVER stored in plain text.
     * BCrypt embeds the salt in the hash itself, so no separate salt column.
     */
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(length = 15)
    private String phone;

    /**
     * Hibernate sets this on INSERT. We never set it manually.
     * `updatable = false` enforces this — even if someone tries to update it,
     * Hibernate ignores the change.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Hibernate sets this on every UPDATE.
     * Note: in MySQL we ALSO have ON UPDATE CURRENT_TIMESTAMP at the DB level.
     * That's belt-and-suspenders — either layer alone would work.
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}