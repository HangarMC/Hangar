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
    public static final String MSG = "message";
    public static final String ARGS = "args";

    public static ModelAndView showAlert(ModelAndView mav, AlertType alertType, String alertMessage, Object...args) {
        Map<String, Object> alerts = (Map<String, Object>) mav.getModelMap().getAttribute("alerts");
        mav.addObject("alerts", createAlert(alerts, alertType, alertMessage, args));
        return mav;
    }

    public static void showAlert(RedirectAttributes attributes, AlertType alertType, String alertMsg, Object...args) {
        Map<String, Object> alerts =  (Map<String, Object>) attributes.getFlashAttributes().get("alerts");
        attributes.addFlashAttribute("alerts", createAlert(alerts, alertType, alertMsg, args));
    }

    private static Map<String, Object> createAlert(Map<String, Object> alerts, AlertType alertType, String alertMessage, Object...args){
        if (alerts == null) {
            alerts = new HashMap<>();
        }
        Map<String, Object> thisAlert = new HashMap<>();
        thisAlert.put(MSG, alertMessage);
        thisAlert.put(ARGS, args);
        return Map.of(alertType.name().toLowerCase(), thisAlert);
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
