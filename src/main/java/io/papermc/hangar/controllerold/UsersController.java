package io.papermc.hangar.controllerold;

import io.papermc.hangar.config.hangar.HangarConfig;
import io.papermc.hangar.db.modelold.UsersTable;
import io.papermc.hangar.model.common.NamedPermission;
import io.papermc.hangar.model.internal.sso.AuthUser;
import io.papermc.hangar.securityold.annotations.GlobalPermission;
import io.papermc.hangar.service.internal.auth.SSOService;
import io.papermc.hangar.service.internal.auth.SSOService.SignatureException;
import io.papermc.hangar.serviceold.UserService;
import io.papermc.hangar.util.Routes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;

@Controller("oldUsersController")
public class UsersController extends HangarController {

    private final HangarConfig hangarConfig;
    private final UserService userService;
    private final SSOService ssoService;


    @Autowired
    public UsersController(HangarConfig hangarConfig, UserService userService, SSOService ssoService) {
        this.hangarConfig = hangarConfig;
        this.userService = userService;
        this.ssoService = ssoService;
    }

    @GlobalPermission(NamedPermission.EDIT_OWN_USER_SETTINGS)
    @Secured("ROLE_USER")
    @GetMapping("/{user}/settings/lock/{locked}")
    public ModelAndView setLocked(@PathVariable String user, @PathVariable boolean locked, @RequestParam(required = false) String sso, @RequestParam(required = false) String sig) {
        UsersTable curUser = getCurrentUser();
        if (!hangarConfig.fakeUser.isEnabled()) {
            try {
                AuthUser authUser = ssoService.authenticate(sso, sig);
                if (authUser == null || authUser.getId() != curUser.getId()) {
                    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
                }
            } catch (SignatureException e) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
            }
        }

        if (!locked) {
            // TODO email!
        }
        userService.setLocked(curUser, locked);
        return Routes.USERS_SHOW_PROJECTS.getRedirect(user);
    }

}

