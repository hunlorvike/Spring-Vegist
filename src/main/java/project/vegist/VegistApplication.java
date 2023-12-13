package project.vegist;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class VegistApplication {
	public static void main(String[] args) {
		SpringApplication.run(VegistApplication.class, args);
	}
}
