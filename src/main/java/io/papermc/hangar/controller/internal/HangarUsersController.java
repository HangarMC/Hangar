package io.papermc.hangar.controller.internal;

import io.papermc.hangar.model.internal.HangarUser;
import io.papermc.hangar.modelold.ApiAuthInfo;
import io.papermc.hangar.service.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping(path = "/api/internal", produces = MediaType.APPLICATION_JSON_VALUE, method = { RequestMethod.GET, RequestMethod.POST })
@PreAuthorize("@authenticationService.handleApiRequest(T(io.papermc.hangar.model.Permission).EditOwnUserSettings, T(io.papermc.hangar.controller.extras.ApiScope).ofGlobal())")
public class HangarUsersController {

    private final UsersService usersService;
    private final ApiAuthInfo apiAuthInfo;

    @Autowired
    public HangarUsersController(UsersService usersService, ApiAuthInfo apiAuthInfo) {
        this.usersService = usersService;
        this.apiAuthInfo = apiAuthInfo;
    }

    @GetMapping("/users/@me")
    public ResponseEntity<HangarUser> getCurrentUser() {
        return ResponseEntity.ok(usersService.getUser(apiAuthInfo.getUser().getName(), HangarUser.class));
    }
}
