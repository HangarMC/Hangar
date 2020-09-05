package io.papermc.hangar.controller;

import freemarker.ext.beans.BeansWrapperBuilder;
import freemarker.template.Configuration;

import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModelException;
import io.papermc.hangar.config.hangar.HangarConfig;
import io.papermc.hangar.service.MarkdownService;
import io.papermc.hangar.service.UserService;
import io.papermc.hangar.util.TemplateHelper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

import io.papermc.hangar.util.Routes;

public abstract class HangarController {

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
        BeansWrapperBuilder builder = new BeansWrapperBuilder(Configuration.VERSION_2_3_30);
        builder.setExposeFields(true);
        builder.setUseModelCache(true);
        TemplateHashModel staticModels = builder.build().getStaticModels();
        mav.addObject("@helper", staticModels);
        mav.addObject("config", hangarConfig);
        mav.addObject("markdownService", markdownService);
        mav.addObject("rand", ThreadLocalRandom.current());
        mav.addObject("utils", templateHelper);

        try {
            mav.addObject("Routes", staticModels.get("io.papermc.hangar.util.Routes"));
        } catch (TemplateModelException e) {
            e.printStackTrace();
        }

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
