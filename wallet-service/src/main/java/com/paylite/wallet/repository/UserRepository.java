package com.paylite.wallet.repository;

import com.paylite.wallet.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for the User entity.
 *
 * Spring generates the implementation at runtime — we never write SQL or
 * implementation code. We just declare what we want via method names.
 *
 * Inherited from JpaRepository:
 *   save(User), findById(Long), findAll(), deleteById(Long), count(), etc.
 *
 * Custom methods declared below are auto-implemented based on naming convention.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find a user by their email.
     * Spring generates: SELECT * FROM users WHERE email = ?
     *
     * Returns Optional<User> rather than User directly. This is the modern Java way
     * to handle "might be missing" — forces the caller to handle the not-found case.
     * Without Optional, we'd return null and risk NullPointerExceptions everywhere.
     */
    Optional<User> findByEmail(String email);

    /**
     * Check whether a user exists with this email.
     * Spring generates: SELECT COUNT(*) > 0 FROM users WHERE email = ?
     *
     * Faster than findByEmail() when we don't need the full entity —
     * we only get a boolean, not a row materialized into Java.
     * Used in signup to validate email isn't already taken.
     */
    boolean existsByEmail(String email);
}