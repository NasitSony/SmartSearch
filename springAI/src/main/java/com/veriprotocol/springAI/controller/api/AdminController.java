package com.veriprotocol.springAI.controller.api;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.veriprotocol.springAI.controller.api.dto.DocumentStatusDto;
import com.veriprotocol.springAI.core.IngestProducer;
import com.veriprotocol.springAI.persistance.DocumentReadDao;
import com.veriprotocol.springAI.persistance.DocumentWriteDao;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final DocumentWriteDao documentWriteDao;
    private final DocumentReadDao documentReadDao;
    private final IngestProducer ingestProducer;

    @PostMapping("/retry/{id}")
    public ResponseEntity<DocumentStatusDto> retry(@PathVariable("id") String id) {

        // exists?
        var existing = documentReadDao.findStatusById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // only retry if we actually moved it to PENDING
        int updated = documentWriteDao.forceToPending(id);
        if (updated == 1) {
            ingestProducer.sendRetry(id);
        }

        // return current status after attempt
        return documentReadDao.findStatusById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
