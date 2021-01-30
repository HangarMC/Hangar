package io.papermc.hangar.controller.internal;

import io.papermc.hangar.controller.HangarController;
import io.papermc.hangar.model.internal.HangarUser;
import io.papermc.hangar.service.api.UsersApiService;
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
public class HangarUsersController extends HangarController {

    private final UsersApiService usersApiService;


    @Autowired
    public HangarUsersController(UsersApiService usersApiService) {
        this.usersApiService = usersApiService;
    }

    @GetMapping("/users/@me")
    public ResponseEntity<HangarUser> getCurrentUser() {
        return ResponseEntity.ok(usersApiService.getUser(hangarRequest.getUserTable().getName(), HangarUser.class));
    }
}
