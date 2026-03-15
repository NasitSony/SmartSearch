package com.veriprotocol.springAI.controller.api.dto;

import java.time.OffsetDateTime;

public record DocumentStatusDto(
        String id,
        String status,
        int retryCount,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        String lastError,
        String workerId,
        OffsetDateTime processingStartedAt,
        OffsetDateTime nextRetryAt
) {}