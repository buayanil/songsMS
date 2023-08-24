package htwb.ai.songsservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class SongsServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(SongsServiceApplication.class, args);
	}

}
