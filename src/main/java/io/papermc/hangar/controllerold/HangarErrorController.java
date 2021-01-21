package io.papermc.hangar.controllerold;

import io.papermc.hangar.util.Routes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

@Controller
public class HangarErrorController extends HangarController implements ErrorController {

    @RequestMapping("/error")
    public ModelAndView handleError(HttpServletRequest request) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        String errorRequestUri = (String) request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);

//        request.getAttributeNames().asIterator().forEachRemaining(s -> System.out.println(s + ": " + request.getAttribute(s))); // for logging attributes to see what's there
        ModelAndView mav = new ModelAndView("errors/error");
        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());

            if (statusCode == HttpStatus.NOT_FOUND.value()) {
                mav = new ModelAndView("errors/notFound");
            } else if (statusCode == HttpStatus.GATEWAY_TIMEOUT.value() || statusCode == HttpStatus.REQUEST_TIMEOUT.value()) {
                mav = new ModelAndView("errors/timeout");
            } else if ((statusCode == HttpStatus.FORBIDDEN.value() || statusCode == HttpStatus.UNAUTHORIZED.value()) && currentUser.get().isEmpty() && errorRequestUri != null) {
                return Routes.USERS_LOGIN.getRedirect("", "", errorRequestUri);
            }
        }
        return fillModel(mav);
    }

    @Override
    public String getErrorPath() {
        return "/error";
    }
}
