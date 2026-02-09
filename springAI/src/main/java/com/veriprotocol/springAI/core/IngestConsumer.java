package com.veriprotocol.springAI.core;

import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.kafka.support.Acknowledgment;

import com.veriprotocol.springAI.persistance.DocumentReadDao;

//import com.veriprotocol.springAI.core.DocumentService;
//import com.veriprotocol.springAI.core.IngestRequestEvent;
//import com.veriprotocol.springAI.core.WorkerIdentity;
//import com.veriprotocol.springAI.core.IngestMetrics;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class IngestConsumer {

    private final WorkerIdentity workerIdentity;
    private final IngestMetrics ingestMetrics;
    private final DocumentService documentService;
    private final DocumentReadDao documentReadDao;

    public IngestConsumer(
            WorkerIdentity workerIdentity,
            DocumentService documentService,
            IngestMetrics ingestMetrics,
            DocumentReadDao documentReadDao
    ) {
        this.workerIdentity = workerIdentity;
        this.documentService = documentService;
        this.ingestMetrics = ingestMetrics;
        this.documentReadDao = documentReadDao;
    }

    @KafkaListener(
            topics = "${smartsearch.kafka.ingest-topic}",
            groupId = "smartsearch-workers",
            containerFactory = "smartsearchKafkaListenerContainerFactory"
    )
    public void consume(IngestRequestEvent event, Acknowledgment ack) {

        String docId = event.documentId();
        MDC.put("docId", docId);

        try {
            log.info("📥 Received ingest event for docId={}", docId);

            // 1) Optional idempotency check via status
            var statusOpt = documentReadDao.findStatusById(docId);
            if (statusOpt.isEmpty()) {
                throw new IllegalStateException("Doc not found: " + docId);
            }
            var status = statusOpt.get();
            if ("READY".equals(status.status())) { // your DB uses READY, not SUCCESS
                log.info("✅ Already READY, skipping {}", docId);
                return;
            }

            // 2) Claim the lease (prevents double-processing on rebalance/retry)
            String workerId = workerIdentity.getWorkerId();
            boolean claimed = documentService.claimProcessingLease(docId, workerId);
            if (!claimed) {
                log.info("⏭️ Not claimed (already processing or not pending/failed), skipping {}", docId);
                return;
            }

            // 3) Load payload (text) from DB (Kafka carries only docId)
            DocumentReadDao.DocPayload payload = documentReadDao.loadDocPayload(docId);
            String text = payload.text();

            // 4) Do the real work (chunk + embed + write chunks)
            documentService.addDocument(docId, text);

            // 5) Mark READY
            documentService.markReadyDb(docId);
            ack.acknowledge(); // ✅ commit offset ONLY after DB success

            log.info("✅ Ingest complete docId={}", docId);
            ingestMetrics.onSucceeded();

        } catch (Exception e) {
        	log.error("❌ Ingest failed docId={}", docId, e);
            try {
                documentService.markFailedDb(docId, e.getMessage());
            } catch (Exception markErr) {
                log.warn("markFailedDb skipped (DB unavailable) docId={}: {}", docId, markErr.getMessage());
            }
            ingestMetrics.onFailed();
            throw e; // keep retry/DLQ flow
        } finally {
            MDC.clear();
        }
    }
}
