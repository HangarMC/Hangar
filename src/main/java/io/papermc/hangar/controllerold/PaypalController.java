package io.papermc.hangar.controllerold;

import io.papermc.hangar.service.internal.PaypalService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.net.URISyntaxException;

@Controller
public class PaypalController extends HangarController {

    private final PaypalService paypalService;

    public PaypalController(PaypalService paypalService) {
        this.paypalService = paypalService;
    }

    @PostMapping("/paypal/ipn")
    public ResponseEntity<Object> ipn(@RequestBody String ipn) throws URISyntaxException {
        paypalService.handle(ipn);
        return ResponseEntity.ok().build();
    }

}
