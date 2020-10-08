package io.papermc.hangar.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import freemarker.ext.beans.BeansWrapperBuilder;
import freemarker.template.Configuration;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModelException;
import io.papermc.hangar.config.hangar.HangarConfig;
import io.papermc.hangar.db.model.UsersTable;
import io.papermc.hangar.service.MarkdownService;
import io.papermc.hangar.service.UserService;
import io.papermc.hangar.util.TemplateHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.resource.ResourceUrlProvider;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class HangarController {

    @Autowired
    private UserService userService;
    @Autowired
    private HangarConfig hangarConfig;
    @Autowired
    private MarkdownService markdownService;
    @Autowired
    private TemplateHelper templateHelper;
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private HttpServletResponse response;
    @Autowired
    private ResourceUrlProvider resourceUrlProvider;

    @Autowired
    protected Supplier<Optional<UsersTable>> currentUser;

    private static final Pattern NONCE_PATTERN = Pattern.compile("(?<=nonce-)[a-zA-Z0-9]+");

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
        mav.addObject("mapper", mapper);
        mav.addObject("urls", resourceUrlProvider);

        try {
            mav.addObject("Routes", staticModels.get("io.papermc.hangar.util.Routes"));
        } catch (TemplateModelException e) {
            e.printStackTrace();
        }

        // alerts
        if (mav.getModelMap().getAttribute("alerts") == null) {
            mav.addObject("alerts", new HashMap<>());
        }
        mav.addObject("cu", currentUser.get().orElse(null));
        mav.addObject("headerData", userService.getHeaderData());
        if (response.containsHeader("Content-Security-Policy")) {
            Matcher nonceMatcher = NONCE_PATTERN.matcher(response.getHeader("Content-Security-Policy"));
            if (!nonceMatcher.find()) {
                throw new IllegalStateException("Must have script nonce defined");
            }
            String nonce = nonceMatcher.group();
            mav.addObject("nonce", nonce);
        } else {
            mav.addObject("nonce", "missing-csp-header");
        }
        return mav;
    }

    protected UsersTable getCurrentUser() {
        return currentUser.get().orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN));
    }
}
