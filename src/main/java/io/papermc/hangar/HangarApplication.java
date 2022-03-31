package io.papermc.hangar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HangarApplication {

    public static void main(String[] args) {
        SpringApplication.run(HangarApplication.class, args);
    }

}
