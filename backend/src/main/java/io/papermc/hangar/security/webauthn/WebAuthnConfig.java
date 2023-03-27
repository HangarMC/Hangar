package io.papermc.hangar.security.webauthn;

import com.webauthn4j.WebAuthnManager;
import com.webauthn4j.converter.util.ObjectConverter;
import com.webauthn4j.data.AttestationConveyancePreference;
import com.webauthn4j.data.AuthenticatorAttachment;
import com.webauthn4j.data.PublicKeyCredentialParameters;
import com.webauthn4j.data.PublicKeyCredentialType;
import com.webauthn4j.data.attestation.statement.COSEAlgorithmIdentifier;
import com.webauthn4j.data.client.Origin;
import com.webauthn4j.data.client.challenge.Challenge;
import com.webauthn4j.server.ServerProperty;
import com.webauthn4j.springframework.security.WebAuthnAuthenticationProvider;
import com.webauthn4j.springframework.security.WebAuthnAuthenticationToken;
import com.webauthn4j.springframework.security.WebAuthnRegistrationRequestValidator;
import com.webauthn4j.springframework.security.WebAuthnSecurityExpression;
import com.webauthn4j.springframework.security.authenticator.WebAuthnAuthenticatorService;
import com.webauthn4j.springframework.security.challenge.ChallengeRepository;
import com.webauthn4j.springframework.security.challenge.HttpSessionChallengeRepository;
import com.webauthn4j.springframework.security.config.configurers.WebAuthnLoginConfigurer;
import com.webauthn4j.springframework.security.options.AssertionOptionsProvider;
import com.webauthn4j.springframework.security.options.AssertionOptionsProviderImpl;
import com.webauthn4j.springframework.security.options.AttestationOptionsProvider;
import com.webauthn4j.springframework.security.options.AttestationOptionsProviderImpl;
import com.webauthn4j.springframework.security.options.PublicKeyCredentialUserEntityProvider;
import com.webauthn4j.springframework.security.options.RpIdProvider;
import com.webauthn4j.springframework.security.options.RpIdProviderImpl;
import com.webauthn4j.springframework.security.server.ServerPropertyProvider;
import io.papermc.hangar.security.authentication.HangarAuthenticationEntryPoint;
import io.papermc.hangar.security.authentication.user.HangarUserPrincipal;
import io.papermc.hangar.service.TokenService;
import io.papermc.hangar.service.internal.auth.HangarPublicKeyCredentialUserEntityProvider;
import io.papermc.hangar.service.internal.auth.HangarUserDetailService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.authentication.AuthenticationEntryPointFailureHandler;

@Configuration
public class WebAuthnConfig {

    private final TokenService tokenService;
    private final HangarAuthenticationEntryPoint entryPoint;

    public WebAuthnConfig(final TokenService tokenService, final HangarAuthenticationEntryPoint entryPoint) {
        this.tokenService = tokenService;
        this.entryPoint = entryPoint;
    }

    public void configure(final HttpSecurity http, final AuthenticationManager authenticationManager) throws Exception {
        http.apply(WebAuthnLoginConfigurer.webAuthnLogin())
            .loginProcessingUrl("/api/internal/auth/login")
            .successHandler((request, response, authentication) -> {
                if (authentication instanceof final UsernamePasswordAuthenticationToken token) {
                    if (token.getPrincipal() instanceof final HangarUserPrincipal principal) {
                        this.tokenService.issueRefreshToken(principal.getUserTable(), response);
                        principal.setAal(1); // check if email verified, if not, aal = 0
                        return;
                    }
                } else if (authentication instanceof final WebAuthnAuthenticationToken token) {
                    if (token.getPrincipal() instanceof final HangarUserPrincipal principal) {
                        principal.setAal(2);
                        System.out.println("woooo");
                        return;
                    }
                }
                System.out.println("????");
                response.setStatus(401);
            })
            .failureHandler(new AuthenticationEntryPointFailureHandler(this.entryPoint))
            .attestationOptionsEndpoint()
            .processingUrl("/api/internal/webauthn/attestation/options")
            .attestation(AttestationConveyancePreference.NONE)
            .timeout(60000L)
            .rp()
            .name("WebAuthn4J Spring Security Sample") // TODO configure
            .and()
            .pubKeyCredParams(
                new PublicKeyCredentialParameters(PublicKeyCredentialType.PUBLIC_KEY, COSEAlgorithmIdentifier.RS256), // Windows Hello
                new PublicKeyCredentialParameters(PublicKeyCredentialType.PUBLIC_KEY, COSEAlgorithmIdentifier.ES256) // FIDO U2F Key, etc
            )
            .authenticatorSelection()
            .authenticatorAttachment(AuthenticatorAttachment.CROSS_PLATFORM)
            .and()
            .extensions()
            .credProps(true)
            .and()
            .assertionOptionsEndpoint()
            .processingUrl("/api/internal/webauthn/assertion/options")
            .timeout(60000L)
            .and().and()
            .authenticationManager(authenticationManager);
    }

    @Bean
    public WebAuthnAuthenticationProvider webAuthnAuthenticationProvider(final WebAuthnAuthenticatorService authenticatorService, final WebAuthnManager webAuthnManager) {
        return new WebAuthnAuthenticationProvider(authenticatorService, webAuthnManager);
    }

    @Bean
    public WebAuthnRegistrationRequestValidator webAuthnRegistrationRequestValidator(final WebAuthnManager webAuthnManager, final ServerPropertyProvider serverPropertyProvider) {
        return new WebAuthnRegistrationRequestValidator(webAuthnManager, serverPropertyProvider);
    }

    @Bean
    public AuthenticationTrustResolver authenticationTrustResolver() {
        return new AuthenticationTrustResolverImpl();
    }

    @Bean
    public PublicKeyCredentialUserEntityProvider webAuthnUserEntityProvider(final HangarUserDetailService hangarUserDetailService) {
        return new HangarPublicKeyCredentialUserEntityProvider(hangarUserDetailService);
    }

    @Bean
    public ChallengeRepository challengeRepository() {
        return new HttpSessionChallengeRepository();
    }

    @Bean
    public AttestationOptionsProvider attestationOptionsProvider(final RpIdProvider rpIdProvider, final WebAuthnAuthenticatorService webAuthnAuthenticatorService, final ChallengeRepository challengeRepository, final PublicKeyCredentialUserEntityProvider publicKeyCredentialUserEntityProvider) {
        final AttestationOptionsProviderImpl optionsProviderImpl = new AttestationOptionsProviderImpl(rpIdProvider, webAuthnAuthenticatorService, challengeRepository);
        optionsProviderImpl.setPublicKeyCredentialUserEntityProvider(publicKeyCredentialUserEntityProvider);
        return optionsProviderImpl;
    }

    @Bean
    public AssertionOptionsProvider assertionOptionsProvider(final RpIdProvider rpIdProvider, final WebAuthnAuthenticatorService webAuthnAuthenticatorService, final ChallengeRepository challengeRepository) {
        return new AssertionOptionsProviderImpl(rpIdProvider, webAuthnAuthenticatorService, challengeRepository);
    }

    @Bean
    public RpIdProvider rpIdProvider() {
        return new RpIdProviderImpl();
    }

    @Bean
    public ServerPropertyProvider serverPropertyProvider(final RpIdProvider rpIdProvider, final ChallengeRepository challengeRepository) {
        return request -> {
            //final Origin origin = ServletUtil.getOrigin(request); // TODO ????
            final Origin origin = new Origin("http://localhost:3333");
            final String effectiveRpId = rpIdProvider.provide(request);
            final Challenge challenge = challengeRepository.loadOrGenerateChallenge(request);

            return new ServerProperty(origin, effectiveRpId, challenge, null);
        };
    }

    @Bean
    public WebAuthnManager webAuthnManager(final ObjectConverter objectConverter) {
        final WebAuthnManager webAuthnManager = WebAuthnManager.createNonStrictWebAuthnManager(objectConverter);
        webAuthnManager.getAuthenticationDataValidator().setCrossOriginAllowed(true);
        return webAuthnManager;
    }

    @Bean
    public WebAuthnSecurityExpression webAuthnSecurityExpression() {
        return new WebAuthnSecurityExpression();
    }
}
