package me.minidigger.hangar.controller;

import freemarker.ext.beans.BeansWrapperBuilder;
import freemarker.template.Configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import me.minidigger.hangar.config.HangarConfig;
import me.minidigger.hangar.service.MarkdownService;
import me.minidigger.hangar.service.UserService;
import me.minidigger.hangar.util.RouteHelper;

public abstract class HangarController {

    @Autowired
    private RouteHelper routeHelper;
    @Autowired
    private UserService userService;
    @Autowired
    private HangarConfig hangarConfig;
    @Autowired
    private MarkdownService markdownService;

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

        // alerts
        if (mav.getModelMap().getAttribute("alerts") == null) {
            mav.addObject("alerts", new HashMap<>());
        }

        // user data
        mav.addObject("user", userService.getCurrentUser()); // TODO this is wrong
        mav.addObject("cu", userService.getCurrentUser());
        mav.addObject("headerData", userService.getHeaderData());

        return mav;
    }
}
