package com.veriprotocol.springAI;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.veriprotocol.springAI")
@EnableScheduling
@EnableKafka
public class SpringAiApplication {
//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
	public static void main(String[] args) {
		SpringApplication.run(SpringAiApplication.class, args);
	}

}
