package me.minidigger.hangar.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import me.minidigger.hangar.model.ModelData;

@Controller
public class HomeController {

    @GetMapping("/")
    public ModelAndView home() {
        return fillModel( new ModelAndView("home"));
    }

    private ModelAndView fillModel(ModelAndView mav) {
        mav.addObject("modelData", new ModelData());
        return mav;
    }
}
