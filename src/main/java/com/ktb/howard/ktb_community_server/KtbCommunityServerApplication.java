package com.ktb.howard.ktb_community_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class KtbCommunityServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(KtbCommunityServerApplication.class, args);
	}

}
