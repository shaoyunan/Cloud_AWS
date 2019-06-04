package com.csye6225.springbootapi;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import com.csye6225.springbootapi.storageservice.local.StorageProperties;

@SpringBootApplication
@EnableJpaAuditing
@EnableConfigurationProperties(StorageProperties.class)
public class MainApplication extends SpringBootServletInitializer{

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		return builder.sources(MainApplication.class).properties("spring.config.name: csye6225");
	}
	
	public static void main(String[] args) {
		System.setProperty("spring.config.name", "csye6225");
		SpringApplication.run(MainApplication.class, args);
	}

	@Bean
	CommandLineRunner init() {
		return (args) -> {
			//storageService.deleteAll();
			//storageService.init();
		};
	}
}
