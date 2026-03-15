package com.veriprotocol.springAI.core;

import org.springframework.dao.DataAccessException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.veriprotocol.springAI.persistence.DocumentWriteDao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReaperJob {

    private final DocumentWriteDao documentWriteDao;

    @Scheduled(fixedDelay = 60_000, initialDelay = 15_000)
    public void reapStuckProcessing() {
        try {
            int n = documentWriteDao.markStuckProcessingAsFailed(5);
            if (n > 0) {
                log.warn("ReaperJob: marked {} stuck PROCESSING docs as FAILED", n);
            }
        } catch (DataAccessException e) {
            // DB down / connection refused / transient DB issues
            log.warn("ReaperJob skipped (DB unavailable): {}", e.getMostSpecificCause().getMessage());
        } catch (Exception e) {
            log.error("ReaperJob unexpected failure", e);
        }
    }
}
