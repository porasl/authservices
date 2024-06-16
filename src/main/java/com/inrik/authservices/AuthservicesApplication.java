package com.inrik.authservices;

import static com.inrik.authservices.user.Role.ADMIN;
import static com.inrik.authservices.user.Role.MANAGER;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import com.inrik.authservices.auth.AuthenticationService;
import com.inrik.authservices.auth.RegisterRequest;

import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableSwagger2
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
					.password("xxxxx")
					.role(ADMIN)
					.status(true)
					.build();
			System.out.println("Admin token: " + service.register(admin).getAccessToken());

			var manager = RegisterRequest.builder()
					.firstname("manager")
					.lastname("manager")
					.email("info@bazaartoday.com")
					.password("xxxxx")
					.role(MANAGER)
					.status(true)
					.build();
			System.out.println("Manager token: " + service.register(manager).getAccessToken());

		};
	}
	
//	@Bean for Swagger
//	   public Docket productApi() {
//	      return new Docket(DocumentationType.SWAGGER_2).select()
//	         .apis(RequestHandlerSelectors.basePackage("com.tutorialspoint.swaggerdemo")).build();
//	   }
}