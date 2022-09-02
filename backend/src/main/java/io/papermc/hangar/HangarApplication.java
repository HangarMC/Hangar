package io.papermc.hangar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@Import(JdbiBeanFactoryPostProcessor.class)
public class HangarApplication {

    public static void main(String[] args) {
        SpringApplication.run(HangarApplication.class, args);
    }

}
