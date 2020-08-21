package io.papermc.hangar.util;

import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

public class AlertUtil {

    public enum AlertType {
        ERROR,
        SUCCESS,
        INFO,
        WARNING
    }

    public static final String TYPE = "alertType";
    public static final String MSG = "alertMsg";
    public static final String ARGS = "alertArgs";

    public static ModelAndView showAlert(ModelAndView mav, AlertType alertType, String alertMessage, String...args) {
        Map<String, String> alerts = (Map<String, String>) mav.getModelMap().getAttribute("alerts");
        if (alerts == null) {
            alerts = new HashMap<>();
        }
        alerts.put(alertType.name().toLowerCase(), alertMessage);
        mav.addObject("alerts", alerts);
        return mav;
    }

    public static RedirectAttributes showAlert(RedirectAttributes attributes, AlertType alertType, String alertMsg, String...args) {
        attributes.addFlashAttribute(TYPE, alertType);
        attributes.addFlashAttribute(MSG, alertMsg);
        attributes.addFlashAttribute(ARGS, args);
        return attributes;
    }
    // TODO alert args in alert.ftlh
    public static ModelAndView transferAlerts(ModelAndView mav, ModelMap modelMap) {
        if (modelMap.containsAttribute(TYPE) && modelMap.containsAttribute(MSG)) {
            AlertType type = (AlertType) modelMap.getAttribute(TYPE);
            String msg = (String) modelMap.getAttribute(MSG);
            String[] args = null;
            if (modelMap.containsAttribute(ARGS)) {
                args = (String[]) modelMap.getAttribute(ARGS);
            }
            return showAlert(mav, type, msg, args);
        }
        return mav;
    }
}
