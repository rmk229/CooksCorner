package kz.yermek;

import kz.yermek.util.JwtTokenUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import javax.crypto.SecretKey;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "kz.yermek.repositories")
public class CooksCornerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CooksCornerApplication.class, args);
    }
}
