package io.papermc.hangar.securityold;

import io.papermc.hangar.exceptions.HangarException;
import io.papermc.hangar.util.Routes;
import org.springframework.web.servlet.ModelAndView;

@Deprecated
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
