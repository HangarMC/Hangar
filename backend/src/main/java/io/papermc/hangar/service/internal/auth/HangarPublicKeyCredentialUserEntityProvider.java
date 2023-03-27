package io.papermc.hangar.service.internal.auth;

import com.webauthn4j.data.PublicKeyCredentialUserEntity;
import com.webauthn4j.springframework.security.options.PublicKeyCredentialUserEntityProvider;
import io.papermc.hangar.security.authentication.user.HangarUserPrincipal;
import java.math.BigInteger;
import org.springframework.security.core.Authentication;

public class HangarPublicKeyCredentialUserEntityProvider implements PublicKeyCredentialUserEntityProvider {

    private final HangarUserDetailService userDetailService;

    public HangarPublicKeyCredentialUserEntityProvider(final HangarUserDetailService userDetailService) {
        this.userDetailService = userDetailService;
    }

    @Override
    public PublicKeyCredentialUserEntity provide(final Authentication authentication) {
        if(authentication == null){
            return null;
        }
        System.out.println("load public key " + authentication);

        final String username = authentication.getName();
        final HangarUserPrincipal principal = this.userDetailService.loadUserByUsername(username);
        return new PublicKeyCredentialUserEntity(
            BigInteger.valueOf(principal.getUserId()).toByteArray(), // TODO this isn't a good id I guess?
                principal.getUsername(),
                principal.getUsername()
        );
    }
}
