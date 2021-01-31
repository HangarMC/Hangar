package io.papermc.hangar.controller.internal;

import io.papermc.hangar.controller.HangarController;
import io.papermc.hangar.controller.extras.exceptions.HangarApiException;
import io.papermc.hangar.model.internal.user.HangarUser;
import io.papermc.hangar.service.api.UsersApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping(path = "/api/internal", produces = MediaType.APPLICATION_JSON_VALUE, method = { RequestMethod.GET, RequestMethod.POST })
public class HangarUsersController extends HangarController {

    private final UsersApiService usersApiService;

    @Autowired
    public HangarUsersController(UsersApiService usersApiService) {
        this.usersApiService = usersApiService;
    }

    @GetMapping("/users/@me")
    public Object getCurrentUser() {
        if (!hangarRequest.hasUser()) {
            throw new HangarApiException(HttpStatus.UNAUTHORIZED, "Not logged in");
        }
        return ResponseEntity.ok(usersApiService.getUser(hangarRequest.getUserTable().getName(), HangarUser.class));
    }
}
