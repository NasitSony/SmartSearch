package com.veriprotocol.springAI.core;

public record IngestRequestEvent(
    String documentId,
    //String contentHash,
    long requestedAtEpochMs
) {}
