package com.nelson.envmanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.nelson.envmanagement")
public class EnvmanagementApplication {

	public static void main(String[] args) {
		SpringApplication.run(EnvmanagementApplication.class, args);
	}

}
