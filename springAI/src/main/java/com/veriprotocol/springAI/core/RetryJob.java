package com.veriprotocol.springAI.core;

import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.veriprotocol.springAI.persistance.DocumentReadDao;
import com.veriprotocol.springAI.persistance.DocumentWriteDao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class RetryJob {

    private final DocumentReadDao documentReadDao;
    private final DocumentWriteDao documentWriteDao;
    private final IngestProducer ingestProducer;

    // tune these later
    private static final int BATCH_SIZE = 20;
    private static final int MAX_RETRIES = 5;

    @Scheduled(fixedDelay = 30_000, initialDelay = 20_000)
    public void republishFailedDocs() {
        try {
            List<String> ids = documentReadDao.findRetryableFailedDocIds(BATCH_SIZE, MAX_RETRIES);

            if (ids.isEmpty()) {
                return;
            }

            for (String id : ids) {
                try {
                    int updated = documentWriteDao.resetFailedToPending(id);
                    if (updated == 1) {
                        // republish to the SAME ingest topic
                        ingestProducer.sendRetry(id);
                        log.info("RetryJob republished docId={}", id);
                    }
                } catch (DataAccessException e) {
                    // per-doc DB issues (rare, but safe)
                    log.warn("RetryJob DB error for docId={}: {}", id, e.getMostSpecificCause().getMessage());
                } catch (Exception e) {
                    // per-doc unexpected error
                    log.warn("RetryJob failed for docId={}: {}", id, e.getMessage());
                }
            }

        } catch (DataAccessException e) {
            // DB down / connection refused / pool timeout → expected in chaos test
            log.warn("RetryJob skipped (DB unavailable): {}", e.getMostSpecificCause().getMessage());
        } catch (Exception e) {
            log.error("RetryJob unexpected failure", e);
        }
    }
}
