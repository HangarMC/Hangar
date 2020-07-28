package me.minidigger.hangar.util;

import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;

public class AlertUtil {

    public static final String ERROR = "error";

    public static ModelAndView showAlert(ModelAndView mav, String alertType, String alertMessage) {
        Map<String, String> alerts = (Map<String, String>) mav.getModelMap().getAttribute("alerts");
        if (alerts == null) {
            alerts = new HashMap<>();
        }
        alerts.put(alertType, alertMessage);
        mav.addObject("alerts", alerts);
        return mav;
    }
}
