package ua.lyashko.data;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"ua.lyashko.data"})
@EntityScan(basePackages = "ua.lyashko.commons.entity")
public class DataServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(DataServiceApplication.class, args);
    }
}
