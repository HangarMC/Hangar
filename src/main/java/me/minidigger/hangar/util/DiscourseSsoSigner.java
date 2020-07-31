package me.minidigger.hangar.util;

import io.undertow.util.HexConverter;
import me.minidigger.hangar.config.HangarConfig;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Map;

// reference: https://github.com/MiniDigger/HangarAuth/blob/master/spongeauth/sso/discourse_sso.py
@Component
public class DiscourseSsoSigner {

    private final HangarConfig.HangarSsoConfig ssoConfig;

    @Autowired
    public DiscourseSsoSigner(HangarConfig.HangarSsoConfig ssoConfig) {
        this.ssoConfig = ssoConfig;
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

        String querystring = new String(Base64.getDecoder().decode(payload));
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
            sb.append(sb.length() == 0 ? "?" : "&")
                    .append(entry.getKey())
                    .append("=")
                    .append(entry.getValue());
        }

        String encoded = Base64.getEncoder().encodeToString(sb.toString().getBytes());
        return ImmutablePair.of(encoded, sign(encoded));
    }

    public static class SignatureException extends HangarException {
        SignatureException(String payload, String signature) {
            super("error.spongeauth.auth", payload, signature);
        }
    }

}
