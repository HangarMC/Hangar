package io.papermc.hangar.controllerold;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.papermc.hangar.model.common.NamedPermission;
import io.papermc.hangar.securityold.annotations.OrganizationPermission;
import io.papermc.hangar.serviceold.AuthenticationService;
import io.papermc.hangar.util.AlertUtil;
import io.papermc.hangar.util.AlertUtil.AlertType;
import io.papermc.hangar.util.Routes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.ModelAndView;

import java.net.URI;

@Controller("oldOrganizationController")
public class OrganizationController extends HangarController {

    private final AuthenticationService authenticationService;

    @Autowired
    public OrganizationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @OrganizationPermission(NamedPermission.EDIT_SUBJECT_SETTINGS)
    @Secured("ROLE_USER")
    @GetMapping("/organizations/{organization}/settings/avatar")
    public ModelAndView updateAvatar(@PathVariable String organization) {
        try {
            URI uri = authenticationService.changeAvatarUri(getCurrentUser().getName(), organization);
            return Routes.getRedirectToUrl(uri.toString());
        } catch (JsonProcessingException e) {
            ModelAndView mav = Routes.USERS_SHOW_PROJECTS.getRedirect(organization);
            AlertUtil.showAlert(mav, AlertType.ERROR, "organization.avatarFailed");
            return mav;
        }
    }
}
