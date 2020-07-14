package me.minidigger.hangar.controller;

import freemarker.ext.beans.BeansWrapperBuilder;
import freemarker.template.Configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.ModelAndView;

import me.minidigger.hangar.service.UserService;
import me.minidigger.hangar.util.RouteHelper;
import me.minidigger.hangar.util.TemplateHelper;

public abstract class HangarController {

    @Autowired
    private RouteHelper routeHelper;
    @Autowired
    private UserService userService;
    @Autowired
    private TemplateHelper templateHelper;

    protected ModelAndView fillModel(ModelAndView mav) {
        // helpers
        mav.addObject("routes", routeHelper);
        mav.addObject("templateHelper", templateHelper);
        BeansWrapperBuilder builder = new BeansWrapperBuilder(Configuration.VERSION_2_3_30);
        builder.setExposeFields(true);
        builder.setUseModelCache(true);
        mav.addObject("@helper", builder.build().getStaticModels());

        // user data
        mav.addObject("user", userService.getCurrentUser());
        mav.addObject("modelData", userService.getModelData());

        return mav;
    }
}
