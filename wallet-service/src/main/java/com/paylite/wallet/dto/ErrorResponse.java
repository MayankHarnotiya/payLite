package com.paylite.wallet.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Uniform error response shape for the entire API.
 *
 * Every failure (validation error, business rule violation, server error)
 * returns this structure. Clients can rely on `status`, `error`, `message`
 * always being present.
 *
 * @JsonInclude(NON_NULL) means: drop null fields from the JSON output.
 * That way, simple errors don't carry empty `fieldErrors: null`.
 */
@Getter
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private LocalDateTime timestamp;
    private int status;            // HTTP status code as a number, e.g., 409
    private String error;          // HTTP status reason, e.g., "Conflict"
    private String message;        // Human-readable message
    private String path;           // Request path that triggered the error

    /**
     * For validation errors only: which field failed which validation.
     * Example: { "email": "Email format is invalid", "password": "must be ≥8 chars" }
     */
    private Map<String, String> fieldErrors;
}