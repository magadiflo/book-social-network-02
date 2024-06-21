package dev.magadiflo.book.network.app;

import dev.magadiflo.book.network.app.role.Role;
import dev.magadiflo.book.network.app.role.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@EnableJpaAuditing(auditorAwareRef = "auditorAware") //"auditorAware", nombre del método del @Bean auditorAware
@SpringBootApplication
public class BookNetworkBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookNetworkBackendApplication.class, args);
    }

    @Bean
    public CommandLineRunner run(RoleRepository roleRepository) {
        return args -> {
            if (roleRepository.findByName("USER").isEmpty()) {
                Role role = Role.builder().name("USER").build();
                roleRepository.save(role);
            }
        };
    }

}
