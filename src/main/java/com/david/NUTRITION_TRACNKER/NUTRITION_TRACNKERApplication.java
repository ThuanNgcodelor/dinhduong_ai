package com.david.NUTRITION_TRACNKER;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class NUTRITION_TRACNKERApplication {

	public static void main(String[] args) {
		SpringApplication.run(NUTRITION_TRACNKERApplication.class, args);
	}

}
