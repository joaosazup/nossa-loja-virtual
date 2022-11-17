package br.com.zup.edu.nossalojavirtual;

import br.com.zup.edu.nossalojavirtual.users.Password;
import br.com.zup.edu.nossalojavirtual.users.User;
import br.com.zup.edu.nossalojavirtual.users.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

@SpringBootApplication
@EnableFeignClients
public class NossaLojaVirtualApplication {

	public static void main(String[] args) {
		SpringApplication.run(NossaLojaVirtualApplication.class, args);
	}

	@Bean
	@Profile("!test")
	CommandLineRunner runner(UserRepository repository) {
		User buzz = new User("buzz@toystory.com", Password.encode("123456"));
		User woody = new User("woody@toystory.com", Password.encode("123456"));

		return (args) -> {
			repository.save(buzz);
			repository.save(woody);
		};

	}
}
