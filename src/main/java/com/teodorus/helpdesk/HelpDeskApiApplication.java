package com.teodorus.helpdesk;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.teodorus.helpdesk.domain.User;
import com.teodorus.helpdesk.enums.ProfileEnum;
import com.teodorus.helpdesk.repository.UserRepository;

@SpringBootApplication
public class HelpDeskApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(HelpDeskApiApplication.class, args);
	}
	
	@Bean
	CommandLineRunner init(UserRepository repository, PasswordEncoder encoder) {
		return args -> {
			initUsers(repository, encoder);
		};
	}
	
	private void initUsers(UserRepository repository, PasswordEncoder encoder) {
		User admin = new User();
		admin.setEmail("admin@helpdesk.com");
		admin.setPassword(encoder.encode("123456"));
		admin.setProfile(ProfileEnum.ROLE_ADMIN);
		
		User find = repository.findByEmail(admin.getEmail());
		if (find == null) {
			repository.save(admin);
		}
	}

}

