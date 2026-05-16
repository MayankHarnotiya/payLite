package com.paylite.wallet.repository;

import com.paylite.wallet.entity.Transaction;
import com.paylite.wallet.entity.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for Transaction.
 *
 * Inherited methods (free): save, findById, findAll, count, etc.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * Find a transaction by its idempotency key.
     * Used as a last-line dedup check after Redis (defense in depth).
     */
    Optional<Transaction> findByIdempotencyKey(String idempotencyKey);

    /**
     * Has this idempotency key been used? Cheaper than findByIdempotencyKey
     * because Spring generates a COUNT-style query rather than fetching the full row.
     */
    boolean existsByIdempotencyKey(String idempotencyKey);

    /**
     * Transaction history involving a user — either as sender OR recipient.
     * Uses JOIN FETCH on both sender and recipient to avoid N+1 when serializing.
     * Returns paginated results, newest first.
     */
    @Query(
            value = "SELECT t FROM Transaction t " +
                    "JOIN FETCH t.sender " +
                    "JOIN FETCH t.recipient " +
                    "WHERE t.sender = :user OR t.recipient = :user",
            countQuery = "SELECT COUNT(t) FROM Transaction t WHERE t.sender = :user OR t.recipient = :user"
    )
    Page<Transaction> findUserHistory(@Param("user") User user, Pageable pageable);
}