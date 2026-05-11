package com.paylite.wallet.repository;

import com.paylite.wallet.entity.User;
import com.paylite.wallet.entity.Wallet;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for Wallet.
 *
 * Inherited methods (free): save, findById, findAll, deleteById, count, etc.
 *
 * Custom finders below — Spring derives SQL from method names.
 */
@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {

    /**
     * Find a wallet by the owning User entity.
     * Spring generates: SELECT * FROM wallets WHERE user_id = ?
     */
    Optional<Wallet> findByUser(User user);

    /**
     * Find a wallet by the owner's email.
     * Spring traverses the relationship: Wallet → User → email.
     * Generates a JOIN: SELECT w.* FROM wallets w JOIN users u ON w.user_id = u.id WHERE u.email = ?
     */
    Optional<Wallet> findByUserEmail(String email);
}