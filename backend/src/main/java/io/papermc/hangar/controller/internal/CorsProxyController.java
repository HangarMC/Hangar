package io.papermc.hangar.controller.internal;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.security.annotations.ratelimit.RateLimit;
import io.papermc.hangar.security.annotations.unlocked.Unlocked;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.net.MalformedURLException;
import java.net.URL;

@Unlocked
@Controller
@RateLimit(path = "cors")
@RequestMapping("/api/internal/cors")
public class CorsProxyController extends HangarComponent {

    private final RestTemplate restTemplate;

    @Autowired
    public CorsProxyController(final RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/")
    public ResponseEntity<Object> proxy(@RequestParam final String url) throws MalformedURLException {
        final URL u = new URL(url);
        if (this.config.cors.allowedHosts().contains(u.getHost())) {
            try {
                return this.restTemplate.getForEntity(url, Object.class);
            } catch (final HttpStatusCodeException ex) {
                throw new ResponseStatusException(ex.getStatusCode(), ex.getMessage());
            }
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }
}
