package io.papermc.hangar.controller.internal.config;

import io.papermc.hangar.config.hangar.HangarConfig;
import jakarta.servlet.MultipartConfigElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;

@Configuration
public class VersionControllerConfig {
    private final HangarConfig config;

    @Autowired
    public VersionControllerConfig(final HangarConfig config) {
        this.config = config;
    }

    @Bean
    public MultipartConfigElement multipartConfigElement() {
        final MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setMaxFileSize(DataSize.ofBytes(this.config.projects.maxFileSize()));
        factory.setMaxRequestSize(DataSize.ofBytes(this.config.projects.maxTotalFilesSize()));
        return factory.createMultipartConfig();
    }
}
