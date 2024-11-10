package io.papermc.hangar;

import io.papermc.hangar.config.hangar.PagesConfig;
import io.papermc.hangar.security.authentication.HangarPrincipal;
import java.util.Optional;
import org.jdbi.v3.spring.EnableJdbiRepositories;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync
@EnableScheduling
@EnableCaching
@EnableJdbiRepositories
@SpringBootApplication
@ConfigurationPropertiesScan(value = "io.papermc.hangar.config.hangar", basePackageClasses = PagesConfig.class)
public class HangarApplication {

    public static boolean TEST_MODE = false;
    public static Optional<HangarPrincipal> TEST_PRINCIPAL = Optional.empty();

    public static void main(final String[] args) {
        SpringApplication.run(HangarApplication.class, args);
    }
}
