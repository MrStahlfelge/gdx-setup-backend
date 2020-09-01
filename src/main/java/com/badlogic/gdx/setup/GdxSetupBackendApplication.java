package com.badlogic.gdx.setup;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class GdxSetupBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(GdxSetupBackendApplication.class, args);
	}

}
