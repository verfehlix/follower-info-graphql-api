package com.verfehlix.followerinfographqlapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
public class FollowerInfoGraphQLApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(FollowerInfoGraphQLApiApplication.class, args);
	}

	/**
	 * Bean for enabling CORS on every pattern (used for developing)
	 */
	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/**");
			}
		};
	}
}

