package io.papermc.hangar.service.internal.auth;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.papermc.hangar.config.hangar.HangarConfig;
import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.internal.table.auth.UserSignOnDAO;
import io.papermc.hangar.exceptions.HangarException;
import io.papermc.hangar.model.db.auth.UserSignOnTable;
import io.papermc.hangar.model.internal.sso.AuthUser;
import io.papermc.hangar.model.internal.sso.URLWithNonce;
import io.papermc.hangar.util.CryptoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigInteger;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

// reference: https://github.com/MiniDigger/HangarAuth/blob/master/spongeauth/sso/discourse_sso.py
@Service
public class SSOService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SSOService.class);

    private final HangarConfig hangarConfig;
    private final UserSignOnDAO userSignOnDAO;
    private final Cache<String, String> returnUrls;

    @Autowired
    public SSOService(HangarConfig hangarConfig, HangarDao<UserSignOnDAO> userSignOnDAO) {
        this.hangarConfig = hangarConfig;
        this.returnUrls = Caffeine.newBuilder().expireAfterWrite(hangarConfig.sso.getReset()).build();
        this.userSignOnDAO = userSignOnDAO.get();
    }

    private boolean isNonceValid(String nonce) {
        UserSignOnTable userSignOn = userSignOnDAO.getByNonce(nonce);
        if (userSignOn == null) return false;
        long millisSinceCreated = userSignOn.getCreatedAt().until(OffsetDateTime.now(), ChronoUnit.MILLIS);
        if (userSignOn.isCompleted() || millisSinceCreated > 600000) return false;
        userSignOnDAO.markCompleted(userSignOn.getId());
        return true;
    }

    public URLWithNonce getLoginUrl(String returnUrl) {
        return getUrl(returnUrl, hangarConfig.sso.getLoginUrl());
    }

    public URLWithNonce getSignupUrl(String returnUrl) {
        return getUrl(returnUrl, hangarConfig.sso.getSignupUrl());
    }

    public URLWithNonce getVerifyUrl(String returnUrl) {
        return getUrl(returnUrl, hangarConfig.sso.getVerifyUrl());
    }

    private URLWithNonce getUrl(String returnUrl, String baseUrl) {
        String generatedNonce = nonce();
        String payload = generatePayload( returnUrl, generatedNonce);
        String sig = sign(payload);
        String urlEncoded = URLEncoder.encode(payload, StandardCharsets.UTF_8);
        return new URLWithNonce(String.format("%s?sso=%s&sig=%s", hangarConfig.getAuthUrl() + baseUrl, urlEncoded, sig), generatedNonce);
    }

    private String nonce() {
        return new BigInteger(130, ThreadLocalRandom.current()).toString(32);
    }

    private String generatePayload(String returnUrl, String nonce) {
        String payload = String.format("return_sso_url=%s&nonce=%s", returnUrl, nonce);
        return new String(Base64.getEncoder().encode(payload.getBytes(StandardCharsets.UTF_8)));
    }

    // TODO logging
    public AuthUser authenticate(String payload, String sig) {
        Map<String, String> decoded = decode(payload, sig);
        String nonce = decoded.get("nonce");
        long externalId = Long.parseLong(decoded.get("external_id"));
        String username = decoded.get("username");
        String email = decoded.get("email");
        AuthUser authUser = new AuthUser(
                externalId,
                username,
                email,
                decoded.get("avatar_url"),
                decoded.get("language") == null ? Locale.ENGLISH : Locale.forLanguageTag(decoded.get("language")),
                decoded.get("add_groups")
        );
        if (!isNonceValid(nonce)) {
            return null;
        }
        return authUser;
    }

    public UserSignOnTable insert(String nonce) {
        return userSignOnDAO.insert(new UserSignOnTable(nonce));
    }

    private String sign(String payload) {
        try {
            return CryptoUtils.hmacSha256(hangarConfig.sso.getSecret(), payload.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            LOGGER.warn("Error while singing sso key", e);
            throw new HangarException("error.loginFailed");
        }
    }

    private boolean verify(String payload, String signature) {
        return sign(payload).equalsIgnoreCase(signature);
    }

    // reference: unsign
    public Map<String, String> decode(String payload, String signature) {
        if (!verify(payload, signature)) {
            throw new SignatureException(payload, signature);
        }

        String decoded = new String(Base64.getDecoder().decode(payload));
        String querystring = URLDecoder.decode(decoded, StandardCharsets.UTF_8);
        return UriComponentsBuilder.fromUriString("/?" + querystring).build().getQueryParams().toSingleValueMap();
    }

    public String getReturnUrl(String nonce) {
        return returnUrls.getIfPresent(nonce);
    }

    public static class SignatureException extends HangarException {
        SignatureException(String payload, String signature) {
            super("error.spongeauth.auth", payload, signature);
        }
    }
}
