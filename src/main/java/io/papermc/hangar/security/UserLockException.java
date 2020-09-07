package io.papermc.hangar.security;

import io.papermc.hangar.util.HangarException;
import io.papermc.hangar.util.Routes;
import org.springframework.web.servlet.ModelAndView;

public class UserLockException extends HangarException {

    private final Routes redirectRoute;
    private final String[] routeArgs;

    public UserLockException(String messageKey, Routes redirectRoute, String[] routeArgs) {
        super(messageKey);
        this.redirectRoute = redirectRoute;
        this.routeArgs = routeArgs;
    }

    public ModelAndView getRedirectView() {
        return redirectRoute.getRedirect(routeArgs);
    }
}
