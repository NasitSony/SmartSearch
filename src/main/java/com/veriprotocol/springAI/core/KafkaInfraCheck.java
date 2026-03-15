package com.veriprotocol.springAI.core;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.config.KafkaListenerConfigUtils;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaInfraCheck {

    private final ApplicationContext ctx;

    @EventListener(ApplicationReadyEvent.class)
    public void check() {
        String beanName = KafkaListenerConfigUtils.KAFKA_LISTENER_ANNOTATION_PROCESSOR_BEAN_NAME;
        boolean exists = ctx.containsBean(beanName);
        log.info("🧩 Kafka listener annotation processor present? {}", exists);

        if (exists) {
            Object bean = ctx.getBean(beanName);
            log.info("🧩 Processor class: {}", bean.getClass().getName());
        }
    }
}
