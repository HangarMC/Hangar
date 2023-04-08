package io.papermc.hangar.components.auth.config;

import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.data.RelyingPartyIdentity;
import io.papermc.hangar.components.auth.service.WebAuthNService;
import io.papermc.hangar.config.hangar.HangarConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebAuthNConfig {

    private final WebAuthNService webAuthNService;
    private final HangarConfig config;

    public WebAuthNConfig(final WebAuthNService webAuthNService, final HangarConfig config) {
        this.webAuthNService = webAuthNService;
        this.config = config;
    }

    @Bean
    public RelyingPartyIdentity relyingPartyIdentity() {
        return RelyingPartyIdentity.builder()
            .id(this.config.security.rpId())
            .name(this.config.security.rpName())
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
