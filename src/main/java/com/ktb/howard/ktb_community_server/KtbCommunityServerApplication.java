package com.ktb.howard.ktb_community_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class KtbCommunityServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(KtbCommunityServerApplication.class, args);
	}

}
