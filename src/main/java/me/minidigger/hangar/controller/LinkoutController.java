package me.minidigger.hangar.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

@Controller
public class LinkoutController {
    @GetMapping(path = "/linkout", params = "remoteUrl")
    public String linkout(@RequestParam("remoteUrl") String remoteUrl, Model model) {
        model.addAttribute("remoteUrl", remoteUrl);
        return "linkout";
    }
}
