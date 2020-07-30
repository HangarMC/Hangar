package me.minidigger.hangar.util;

import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;

public class AlertUtil {

    public enum AlertType {
        ERROR,
        SUCCESS,
        INFO,
        WARNING
    }

    public static ModelAndView showAlert(ModelAndView mav, AlertType alertType, String alertMessage) {
        Map<String, String> alerts = (Map<String, String>) mav.getModelMap().getAttribute("alerts");
        if (alerts == null) {
            alerts = new HashMap<>();
        }
        alerts.put(alertType.name().toLowerCase(), alertMessage);
        mav.addObject("alerts", alerts);
        return mav;
    }
}
