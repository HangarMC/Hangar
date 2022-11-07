package io.papermc.hangar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@Import(JdbiBeanFactoryPostProcessor.class)
@ConfigurationPropertiesScan("io.papermc.hangar.config.hangar")
public class HangarApplication {

    public static void main(String[] args) {
        SpringApplication.run(HangarApplication.class, args);
    }

}
