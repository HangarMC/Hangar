package io.papermc.hangar.controller.internal;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.service.internal.PaypalService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.net.URISyntaxException;

@Controller
@RequestMapping("/api/internal/paypal")
public class PaypalController extends HangarComponent {

    private final PaypalService paypalService;

    public PaypalController(PaypalService paypalService) {
        this.paypalService = paypalService;
    }

    @PostMapping("/ipn")
    public ResponseEntity<Object> ipn(@RequestBody String ipn) throws URISyntaxException {
        paypalService.handle(ipn);
        return ResponseEntity.ok().build();
    }

}
