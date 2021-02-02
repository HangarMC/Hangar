package io.papermc.hangar.controller;

import io.papermc.hangar.config.hangar.HangarConfig;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class RequestController {

    @Autowired
    protected HangarConfig hangarConfig;

    @Autowired
    protected HttpServletRequest request;

    @Autowired
    protected HttpServletResponse response;
}
