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

    public static Map<String, Object> applyAlert(HashMap<String, Object> input, AlertType alertType, String alertMsg, Object...args) {
        Map<String, Object> alerts = (Map<String, Object>) input.get("alerts");
        if (alerts == null) {
            alerts = new HashMap<>();
        }
        Map<String, Object> thisAlert = new HashMap<>();
        thisAlert.put("message", alertMsg);
        thisAlert.put("args", args);
        alerts.put(alertType.name().toLowerCase(), thisAlert);
        input.put("alerts", alerts);
        return input;
    }

    public static ModelAndView showAlert(ModelAndView mav, AlertType alertType, String alertMessage, Object...args) {
        applyAlert(mav.getModelMap(), alertType, alertMessage, args);
        return mav;
    }

    public static ModelAndView showAlert(ModelAndView mav, AlertType alertType, String alertMessage, String... args) {
        applyAlert(mav.getModelMap(), alertType, alertMessage, (Object[]) args);
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
        } else if (modelMap.containsAttribute("alerts")) {
            mav.addObject("alerts", modelMap.getAttribute("alerts"));
        }
        return mav;
    }
}
