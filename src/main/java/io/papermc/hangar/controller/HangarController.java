package io.papermc.hangar.controller;

import freemarker.ext.beans.BeansWrapperBuilder;
import freemarker.template.Configuration;

import io.papermc.hangar.config.hangar.HangarConfig;
import io.papermc.hangar.service.MarkdownService;
import io.papermc.hangar.service.UserService;
import io.papermc.hangar.util.TemplateHelper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

import io.papermc.hangar.util.RouteHelper;

public abstract class HangarController {

    @Autowired
    private RouteHelper routeHelper;
    @Autowired
    private UserService userService;
    @Autowired
    private HangarConfig hangarConfig;
    @Autowired
    private MarkdownService markdownService;
    @Autowired
    private TemplateHelper templateHelper;

    protected ModelAndView fillModel(ModelAndView mav) {
        // helpers
        mav.addObject("routes", routeHelper);
        BeansWrapperBuilder builder = new BeansWrapperBuilder(Configuration.VERSION_2_3_30);
        builder.setExposeFields(true);
        builder.setUseModelCache(true);
        mav.addObject("@helper", builder.build().getStaticModels());
        mav.addObject("config", hangarConfig);
        mav.addObject("markdownService", markdownService);
        mav.addObject("rand", ThreadLocalRandom.current());
        mav.addObject("utils", templateHelper);

        // alerts // commented it out for now, so that it doesnt override the redirect's alerts
//        if (mav.getModelMap().getAttribute("alerts") == null) {
//            mav.addObject("alerts", new HashMap<>());
//        }

        // user data
        mav.addObject("user", userService.getCurrentUser()); // TODO this is wrong
        mav.addObject("cu", userService.getCurrentUser());
        mav.addObject("headerData", userService.getHeaderData());

        return mav;
    }
}
