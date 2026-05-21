package com.paylite.wallet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Paginated wrapper for transaction history.
 *
 * Why a custom wrapper instead of returning Spring's Page<T> directly?
 * Page<T> serializes with internal Spring metadata (sort details, pageable
 * object) that clutters the API response. This DTO exposes only what the
 * client needs: the items, page number, size, and total counts.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionHistoryResponse {

    private List<TransactionItemResponse> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
}