package com.veriprotocol.springAI.controller.api;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.veriprotocol.springAI.persistence.DocumentReadDao;

@RestController
@RequestMapping("/api")
public class SystemController {

    private final DocumentReadDao documentReadDao;

    public SystemController(DocumentReadDao documentReadDao) {
        this.documentReadDao = documentReadDao;
    }

    @GetMapping("/system/pressure")
    public Map<String, Object> pressure() {
        return Map.of(
            "pending", documentReadDao.countByStatus("PENDING"),
            "processing", documentReadDao.countByStatus("PROCESSING"),
            "failed", documentReadDao.countByStatus("FAILED"),
            "ready", documentReadDao.countByStatus("READY")
        );
    }
}

