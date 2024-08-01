package io.papermc.hangar.controller;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.security.annotations.Anyone;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@Anyone
@RestController
@RequestMapping("/.well-known")
public class WellKnownController extends HangarComponent {

    @GetMapping(path = "/change-password", produces = MediaType.TEXT_PLAIN_VALUE)
    public ModelAndView changePassword() {
        return new ModelAndView("redirect:/auth/settings/account");
    }

    @GetMapping(path = "/security.txt", produces = MediaType.TEXT_PLAIN_VALUE)
    public String security() {
        return """
            Contact: mailto:core@papermc.io
            Preferred-Languages: en,de
            """;
    }
}
