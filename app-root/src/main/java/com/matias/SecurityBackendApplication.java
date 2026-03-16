package com.matias;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.matias")
@EnableJpaRepositories(basePackages = "com.matias.database.repository")
@EntityScan(basePackages = "com.matias.database.entity")
@EnableScheduling
public class SecurityBackendApplication {
	public static void main(String[] args) {
		SpringApplication.run(SecurityBackendApplication.class, args);
	}
}
