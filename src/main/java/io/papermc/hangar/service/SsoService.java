package io.papermc.hangar.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.papermc.hangar.config.hangar.HangarConfig;
import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.UserSignOnDao;
import io.papermc.hangar.db.model.UserSignOnsTable;
import io.papermc.hangar.exceptions.HangarException;
import io.papermc.hangar.service.sso.AuthUser;
import io.papermc.hangar.service.sso.UrlWithNonce;
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
public class SsoService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SsoService.class);

    private final HangarConfig hangarConfig;
    private final HangarDao<UserSignOnDao> userSignOnDao;
    private final Cache<String, String> returnUrls;

    @Autowired
    public SsoService(HangarConfig hangarConfig, HangarDao<UserSignOnDao> userSignOnDao) {
        this.hangarConfig = hangarConfig;
        this.returnUrls = Caffeine.newBuilder().expireAfterWrite(hangarConfig.sso.getReset()).build();
        this.userSignOnDao = userSignOnDao;
    }

    private boolean isNonceValid(String nonce) {
        UserSignOnsTable userSignOn = userSignOnDao.get().getByNonce(nonce);
        if (userSignOn == null) return false;
        long millisSinceCreated = userSignOn.getCreatedAt().until(OffsetDateTime.now(), ChronoUnit.MILLIS);
        if (userSignOn.getIsCompleted() || millisSinceCreated > 600000) return false;
        userSignOnDao.get().markCompleted(userSignOn.getId());
        return true;
    }

    public UrlWithNonce getLoginUrl(String returnUrl) {
        return getUrl(returnUrl, hangarConfig.sso.getLoginUrl());
    }

    public UrlWithNonce getSignupUrl(String returnUrl) {
        return getUrl(returnUrl, hangarConfig.sso.getSignupUrl());
    }

    public UrlWithNonce getVerifyUrl(String returnUrl) {
        return getUrl(returnUrl, hangarConfig.sso.getVerifyUrl());
    }

    private UrlWithNonce getUrl(String returnUrl, String baseUrl) {
        String generatedNonce = nonce();
        String payload = generatePayload( returnUrl, generatedNonce);
        String sig = sign(payload);
        String urlEncoded = URLEncoder.encode(payload, StandardCharsets.UTF_8);
        return new UrlWithNonce(String.format("%s?sso=%s&sig=%s", hangarConfig.getAuthUrl() + baseUrl, urlEncoded, sig), generatedNonce);
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

    public UserSignOnsTable insert(String nonce) {
        return userSignOnDao.get().insert(new UserSignOnsTable(nonce));
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
