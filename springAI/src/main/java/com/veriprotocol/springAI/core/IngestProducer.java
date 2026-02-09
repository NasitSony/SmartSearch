package com.veriprotocol.springAI.core;




import java.time.Instant;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class IngestProducer {

  //private final KafkaTemplate<String, IngestRequestEvent> kafkaTemplate;
  //private final String topic;

  /*public IngestProducer(
      KafkaTemplate<String, IngestRequestEvent> kafkaTemplate,
      @Value("${smartsearch.kafka.ingest-topic}") String topic
  ) {
    this.kafkaTemplate = kafkaTemplate;
    this.topic = topic;
  }*/

  //public void send(String docId, String content, String contentHash) {
   // kafkaTemplate.send(topic, docId, new IngestRequestEvent(docId, content, contentHash, Instant.now()));
  //}
  private final KafkaTemplate<String, Object> kafkaTemplate;


  @Value("${smartsearch.kafka.ingest-topic}")
  private String topic;

  //private final String topic = "${smartsearch.kafka.ingest-topic}"; // or from config

  public void publish(String docId, String contentHash) {

        IngestRequestEvent event =
            new IngestRequestEvent(docId, contentHash, System.currentTimeMillis());

        kafkaTemplate.send(topic, docId, event);
  }

  public void send(String docId, String contentHash) {
    kafkaTemplate.send(
        topic,
        docId,
        new IngestRequestEvent(docId, contentHash, System.currentTimeMillis())
    );
}

public void sendRetry(String docId) {
    kafkaTemplate.send(
        topic,
        docId,
        new IngestRequestEvent(docId, null, System.currentTimeMillis())
    );
}
  //public void sendRetry(String docId) {
	    // simplest: send only docId; consumer loads text/hash from DB
	   // kafkaTemplate.send(topic, docId, new IngestRequestEvent(docId, null, null, Instant.now()));
	//}



}