package com.matias;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.matias")
@EnableJpaRepositories(basePackages = "com.matias.database.repository")
@EntityScan(basePackages = "com.matias.database.entity")
public class SecurityBackendApplication {
	public static void main(String[] args) {
		SpringApplication.run(SecurityBackendApplication.class, args);
	}
}