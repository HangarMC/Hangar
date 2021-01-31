package io.papermc.hangar.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import io.papermc.hangar.config.hangar.HangarConfig;

@Service
public class TokenService {

    private final String issuer;
    private final String secret;
    private final int expiry;

    private JWTVerifier verifier;
    private Algorithm algo;

    @Autowired
    public TokenService(HangarConfig config) {
        this.issuer = config.getSecurity().getTokenIssuer();
        this.secret = config.getSecurity().getTokenSecret();
        this.expiry = config.getSecurity().getTokenExpiry();
    }

    public DecodedJWT verify(String token) {
        return getVerifier().verify(token);
    }

    public String expiring(UUID id, List<String> roles) {
        return JWT.create()
                .withIssuer(issuer)
                .withExpiresAt(new Date(Instant.now().plusSeconds(expiry).toEpochMilli()))
                .withSubject(id.toString())
                .withClaim("roles", roles)
                .sign(getAlgo());
    }

    private JWTVerifier getVerifier() {
        if (verifier == null) {
            verifier = JWT.require(getAlgo())
                    .withIssuer(issuer)
                    .build();
        }
        return verifier;
    }

    private Algorithm getAlgo() {
        if (algo == null) {
            algo = Algorithm.HMAC256(secret);
        }
        return algo;
    }
}
