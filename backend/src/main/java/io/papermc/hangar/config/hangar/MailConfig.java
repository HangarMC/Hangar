package io.papermc.hangar.config.hangar;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "hangar.mail")
public record MailConfig(String from, String user, String pass, String host, int port, boolean dev) {
}
