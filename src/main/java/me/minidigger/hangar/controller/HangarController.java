package me.minidigger.hangar.controller;

import org.springframework.web.servlet.ModelAndView;

import me.minidigger.hangar.model.ModelData;

public abstract class HangarController {

    protected ModelAndView fillModel(ModelAndView mav) {
        mav.addObject("modelData", new ModelData());
        return mav;
    }
}
