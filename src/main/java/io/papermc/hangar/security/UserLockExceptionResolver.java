package io.papermc.hangar.security;

import io.papermc.hangar.util.AlertUtil;
import io.papermc.hangar.util.AlertUtil.AlertType;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class UserLockExceptionResolver implements HandlerExceptionResolver {
    @Override
    public ModelAndView resolveException(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, Object handler, @NotNull Exception ex) {

        if (ex instanceof UserLockException) {
            UserLockException exception = (UserLockException) ex;
            AlertUtil.applyAlert(RequestContextUtils.getOutputFlashMap(request), AlertType.ERROR, exception.getMessageKey());
            return exception.getRedirectView();
        }
        return null;
    }
}
