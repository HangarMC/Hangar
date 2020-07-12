package me.minidigger.hangar.controller;

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

        ModelAndView mav = new ModelAndView("errors/error");
        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());

            if (statusCode == HttpStatus.NOT_FOUND.value()) {
                mav = new ModelAndView("errors/notFound");
            } else if (statusCode == HttpStatus.GATEWAY_TIMEOUT.value() || statusCode == HttpStatus.REQUEST_TIMEOUT.value()) {
                mav = new ModelAndView("errors/timeout");
            }
        }
        return fillModel(mav);
    }

    @Override
    public String getErrorPath() {
        return "/error";
    }
}
