package io.papermc.hangar.components.auth.config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import io.papermc.hangar.HangarComponent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JWTConfig extends HangarComponent {

    @Bean
    public JWTVerifier jwtVerifier(final Algorithm algorithm) {
        return JWT.require(algorithm)
            .acceptLeeway(10)
            .withIssuer(this.config.security.tokenIssuer())
            .build();
    }

    @Bean
    public Algorithm algorithm() {
        return Algorithm.HMAC256(this.config.security.tokenSecret());
    }
}
