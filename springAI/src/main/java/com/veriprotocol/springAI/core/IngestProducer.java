package com.veriprotocol.springAI.core;




import java.time.Instant;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class IngestProducer {

  
  private final KafkaTemplate<String, Object> kafkaTemplate;


  @Value("${smartsearch.kafka.ingest-topic}")
  private String topic;

  //private final String topic = "${smartsearch.kafka.ingest-topic}"; // or from config

  public void publish(String docId) {

        IngestRequestEvent event =
            new IngestRequestEvent(docId, System.currentTimeMillis());

        kafkaTemplate.send(topic, docId, event);
  }

  public void send(String docId) {
    kafkaTemplate.send(
        topic,
        docId,
        new IngestRequestEvent(docId, System.currentTimeMillis())
    );
}

public void sendRetry(String docId) {
    kafkaTemplate.send(
        topic,
        docId,
        new IngestRequestEvent(docId, System.currentTimeMillis())
    );
}


}