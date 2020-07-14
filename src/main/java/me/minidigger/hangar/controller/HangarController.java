package me.minidigger.hangar.controller;

import freemarker.ext.beans.BeansWrapperBuilder;
import freemarker.template.Configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.ModelAndView;

import me.minidigger.hangar.model.ModelData;
import me.minidigger.hangar.util.RouteHelper;

public abstract class HangarController {

    @Autowired
    private RouteHelper routeHelper;

    protected ModelAndView fillModel(ModelAndView mav) {
        mav.addObject("modelData", new ModelData());
        mav.addObject("routes", routeHelper);
        BeansWrapperBuilder builder = new BeansWrapperBuilder(Configuration.VERSION_2_3_30);
        builder.setExposeFields(true);
        builder.setUseModelCache(true);
        mav.addObject("@helper", builder.build().getStaticModels());
        return mav;
    }
}
