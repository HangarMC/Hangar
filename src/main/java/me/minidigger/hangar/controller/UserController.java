package me.minidigger.hangar.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class UserController extends HangarController {

    @GetMapping("/staff")
    public ModelAndView staff() {
        return fillModel( new ModelAndView("users/staff"));
    }
}
