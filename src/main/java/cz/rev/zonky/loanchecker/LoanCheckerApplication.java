package cz.rev.zonky.loanchecker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

/**
 * Represents application for checking new loans.
 *
 * @author Petr Stahl [petr.stahl@gmail.com]
 */
@SpringBootApplication
@EnableScheduling
public class LoanCheckerApplication {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

    public static void main(String[] args) {
        SpringApplication.run(LoanCheckerApplication.class, args);
    }

}
