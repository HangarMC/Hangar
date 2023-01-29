package io.papermc.hangar.controller.internal;

import io.papermc.hangar.model.common.NamedPermission;
import io.papermc.hangar.security.annotations.permission.PermissionRequired;
import io.papermc.hangar.security.annotations.ratelimit.RateLimit;
import io.papermc.hangar.security.annotations.unlocked.Unlocked;
import io.papermc.hangar.service.internal.FakeDataService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

@Unlocked
@Controller
@RateLimit(path = "admin")
@RequestMapping("/api/internal/fakeData")
public class FakeDataController {

    private final FakeDataService fakeDataService;

    public FakeDataController(final FakeDataService fakeDataService) {
        this.fakeDataService = fakeDataService;
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    @PermissionRequired(NamedPermission.MANUAL_VALUE_CHANGES)
    public void generateFakeData(@RequestParam final int users, @RequestParam final int projectsPerUser) {
        this.fakeDataService.generate(users, projectsPerUser);
    }
}
