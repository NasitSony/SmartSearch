package com.veriprotocol.springAI.core;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaListenerDebug {

    private final KafkaListenerEndpointRegistry registry;

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        if (registry.getListenerContainers().isEmpty()) {
            log.error("❌ No Kafka listener containers found. @KafkaListener not registered?");
            return;
        }
        registry.getListenerContainers().forEach(c ->
            log.info("🎧 Kafka container: id={} running={} groupId={} topics={}",
                c.getListenerId(), c.isRunning(), c.getGroupId(), c.getContainerProperties().getTopics())
        );
    }
}
