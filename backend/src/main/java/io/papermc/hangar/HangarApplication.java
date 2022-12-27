package io.papermc.hangar;

import io.papermc.hangar.config.hangar.PagesConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@Import(JdbiBeanFactoryPostProcessor.class)
@ConfigurationPropertiesScan(value = "io.papermc.hangar.config.hangar", basePackageClasses = PagesConfig.class)
public class HangarApplication {

    public static void main(final String[] args) {
        SpringApplication.run(HangarApplication.class, args);
    }

}
