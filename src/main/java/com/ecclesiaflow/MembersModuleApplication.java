package com.ecclesiaflow;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.Transactional;

@SpringBootApplication
public class MembersModuleApplication implements CommandLineRunner {


	public static void main(String[] args) {
		SpringApplication.run(MembersModuleApplication.class, args);
	}

	@Override
	@Transactional
	public void run(String... args) {

	}
}