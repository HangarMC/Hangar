package me.minidigger.hangar.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.undertow.util.HexConverter;
import me.minidigger.hangar.config.hangar.HangarConfig;
import me.minidigger.hangar.util.HangarException;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Map;

// reference: https://github.com/MiniDigger/HangarAuth/blob/master/spongeauth/sso/discourse_sso.py
@Service
public class SsoService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SsoService.class);

    private final HangarConfig hangarConfig;
    private final Cache<String, String> returnUrls;

    @Autowired
    public SsoService(HangarConfig hangarConfig) {
        this.hangarConfig = hangarConfig;
        this.returnUrls = Caffeine.newBuilder().expireAfterWrite(hangarConfig.sso.getReset()).build();
    }

    private String sign(String payload) {
        try {
            Mac hasher = Mac.getInstance("HmacSHA256");
            hasher.init(new SecretKeySpec(hangarConfig.sso.getSecret().getBytes(), "HmacSHA256"));
            byte[] hash = hasher.doFinal(payload.getBytes());
            return HexConverter.convertToHexString(hash);
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

    /**
     * @param payloadData Parameters to encode and sign
     * @return A pair of payload and signature
     */
    public Pair<String, String> encode(Map<String, String> payloadData) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : payloadData.entrySet()) {
            sb.append(sb.length() == 0 ? "" : "&")
                    .append(entry.getKey())
                    .append("=")
                    .append(entry.getValue());
        }

        String encoded = Base64.getEncoder().encodeToString(sb.toString().getBytes());
        return ImmutablePair.of(encoded, sign(encoded));
    }

    /**
     * @param returnUrl The URL to return to after login
     * @return A randomly-generated nonce to pass to SSO provider
     */
    public String setReturnUrl(String returnUrl) {
        String nonce = RandomStringUtils.randomAlphanumeric(32);
        returnUrls.put(nonce, returnUrl);
        return nonce;
    }

    public String getReturnUrl(String nonce) {
        return returnUrls.getIfPresent(nonce);
    }

    public void clearReturnUrl(String nonce) {
        returnUrls.invalidate(nonce);
    }

    public String generateAuthReturnUrl(String returnUrl) {
        String nonce = setReturnUrl(returnUrl);
        return ServletUriComponentsBuilder.fromCurrentRequestUri()
                .replaceQuery("")
                .build().toString() + "&nonce=" + nonce; // spongeauth doesn't like properly-formatted query strings, so we have to append this ourselves
    }

    public String getAuthLoginUrl(String payload, String signature) {
        return UriComponentsBuilder.fromHttpUrl(hangarConfig.getAuthUrl() + hangarConfig.sso.getLoginUrl())
                .queryParam("sso", payload)
                .queryParam("sig", signature)
                .build().toString();
    }

    public String getAuthSignupUrl(String payload, String signature) {
        return UriComponentsBuilder.fromHttpUrl(hangarConfig.getAuthUrl() + hangarConfig.sso.getSignupUrl())
                .queryParam("sso", payload)
                .queryParam("sig", signature)
                .build().toString();
    }

    public String getAuthVerifyUrl(String payload, String signature) {
        return UriComponentsBuilder.fromHttpUrl(hangarConfig.getAuthUrl() + hangarConfig.sso.getVerifyUrl())
                .queryParam("sso", payload)
                .queryParam("sig", signature)
                .build().toString();
    }

    public String getAuthLogoutUrl() {
        return UriComponentsBuilder.fromHttpUrl(hangarConfig.getAuthUrl() + hangarConfig.sso.getLogoutUrl())
                .build().toString();
    }

    public static class SignatureException extends HangarException {
        SignatureException(String payload, String signature) {
            super("error.spongeauth.auth", payload, signature);
        }
    }

}
