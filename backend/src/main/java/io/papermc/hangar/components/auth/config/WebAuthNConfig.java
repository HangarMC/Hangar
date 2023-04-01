package io.papermc.hangar.components.auth.config;

import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.data.RelyingPartyIdentity;
import io.papermc.hangar.components.auth.service.WebAuthNService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebAuthNConfig {

    private final WebAuthNService webAuthNService;

    public WebAuthNConfig(final WebAuthNService webAuthNService) {
        this.webAuthNService = webAuthNService;
    }

    @Bean
    public RelyingPartyIdentity relyingPartyIdentity() {
        // TODO get from config
        return RelyingPartyIdentity.builder()
            .id("localhost")
            .name("Hangar")
            .build();
    }

    @Bean
    public RelyingParty relyingParty() {
        return RelyingParty.builder()
            .identity(this.relyingPartyIdentity())
            .credentialRepository(this.webAuthNService)
            .allowOriginPort(true)
            .allowOriginSubdomain(true)
            .build();
    }
}
