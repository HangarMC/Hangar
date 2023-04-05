package io.papermc.hangar.components.auth.service;

import io.papermc.hangar.util.CryptoUtils;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class HibpService {

    private final RestTemplate restTemplate;

    public HibpService(final RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public int getBreachAmount(final String password) {
        final String hashedPassword = CryptoUtils.sha1ToHex(password.getBytes(StandardCharsets.UTF_8));
        final String prefix = hashedPassword.substring(0, 5);
        final String suffix = hashedPassword.substring(5).toUpperCase();

        final HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "Hangar/1.0 #https://github.com/HangarMC/Hangar");
        final ResponseEntity<String> response = this.restTemplate.exchange("https://api.pwnedpasswords.com/range/" + prefix, HttpMethod.GET, new HttpEntity<>(headers), String.class);
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            final Optional<String> match = Arrays.stream(response.getBody().split("\r\n")).filter(s -> s.startsWith(suffix)).findAny();
            if (match.isPresent()) {
                return Integer.parseInt(match.get().split(":")[1]);
            }
        }
        return -1;
    }
}
