package me.minidigger.hangar.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.undertow.util.HexConverter;
import me.minidigger.hangar.config.HangarConfig;
import me.minidigger.hangar.util.HangarException;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Map;

// reference: https://github.com/MiniDigger/HangarAuth/blob/master/spongeauth/sso/discourse_sso.py
@Service
public class SsoService {

    private final HangarConfig.HangarSsoConfig ssoConfig;
    private final Cache<String, String> returnUrls;

    @Autowired
    public SsoService(HangarConfig.HangarSsoConfig ssoConfig) {
        this.ssoConfig = ssoConfig;
        this.returnUrls = Caffeine.newBuilder().expireAfterWrite(ssoConfig.getTimeout()).build();
    }

    private String sign(String payload) {
        try {
            Mac hasher = Mac.getInstance("HmacSHA256");
            hasher.init(new SecretKeySpec(ssoConfig.getSecret().getBytes(), "HmacSHA256"));
            byte[] hash = hasher.doFinal(payload.getBytes());
            return HexConverter.convertToHexString(hash);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
            return null;
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
        // TODO: prepending "/?" is a hack
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

//        String queryString = URLEncoder.encode(sb.toString(), StandardCharsets.UTF_8);
//        String encoded = Base64.getEncoder().encodeToString(queryString.getBytes());
        String encoded = Base64.getEncoder().encodeToString(sb.toString().getBytes());
        return ImmutablePair.of(encoded, sign(encoded));
    }

    /**
     * @param returnUrl The URL to return to after login
     * @return A randomly-generated nonce to pass to SSO provider
     */
    public String setReturnUrl(String returnUrl) {
        // TODO: I have no idea if this is a good way to generate the nonce
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

    public static class SignatureException extends HangarException {
        SignatureException(String payload, String signature) {
            super("error.spongeauth.auth", payload, signature);
        }
    }

}
