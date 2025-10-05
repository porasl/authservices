package com.porasl.authservices;

import static com.porasl.authservices.user.Role.ADMIN;
import static com.porasl.authservices.user.Role.MANAGER;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import com.porasl.authservices.auth.RegisterRequest;
import com.porasl.authservices.service.AuthenticationService;

@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class AuthservicesApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthservicesApplication.class, args);
	}

	@Bean
	public CommandLineRunner commandLineRunner(AuthenticationService service) {
		return args -> {
			var admin = RegisterRequest.builder()
					.firstname("Admin")
					.lastname("Admin")
					.email("info@inrik.com")
					.password("passw0rd")
					.role(ADMIN)
					.status(true)
					.build();
			System.out.println("Admin token: " + service.register(admin).getAccessToken());

			var manager = RegisterRequest.builder()
					.firstname("manager")
					.lastname("manager")
					.email("info@bazaartoday.com")
					.password("passw0rd")
					.role(MANAGER)
					.status(true)
					.build();
			System.out.println("Manager token: " + service.register(manager).getAccessToken());

		};
	}
	
}