package io.papermc.hangar.controller.internal;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.security.annotations.ratelimit.RateLimit;
import io.papermc.hangar.service.internal.PaypalService;
import java.net.URISyntaxException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/internal/paypal")
public class PaypalController extends HangarComponent {

    private final PaypalService paypalService;

    public PaypalController(final PaypalService paypalService) {
        this.paypalService = paypalService;
    }

    /*@RateLimit(overdraft = 5, refillTokens = 1, refillSeconds = 3)
    @PostMapping("/ipn")
    public ResponseEntity<Object> ipn(@RequestBody final String ipn) throws URISyntaxException {
        this.paypalService.handle(ipn);
        return ResponseEntity.ok().build();
    }*/
}
