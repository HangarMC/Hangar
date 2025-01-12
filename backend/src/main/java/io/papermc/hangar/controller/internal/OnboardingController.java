package io.papermc.hangar.controller.internal;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.components.auth.model.dto.SignupForm;
import io.papermc.hangar.components.auth.service.AuthService;
import io.papermc.hangar.model.common.NamedPermission;
import io.papermc.hangar.model.common.roles.GlobalRole;
import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.model.db.roles.GlobalRoleTable;
import io.papermc.hangar.security.annotations.permission.PermissionRequired;
import io.papermc.hangar.security.annotations.ratelimit.RateLimit;
import io.papermc.hangar.security.annotations.unlocked.Unlocked;
import io.papermc.hangar.service.internal.FakeDataService;
import io.papermc.hangar.service.internal.perms.roles.GlobalRoleService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RateLimit(path = "admin")
@RequestMapping("/api/internal/onboarding")
public class OnboardingController extends HangarComponent {

    private final FakeDataService fakeDataService;
    private final AuthService authService;
    private final GlobalRoleService globalRoleService;

    public OnboardingController(final FakeDataService fakeDataService, final AuthService authService, final GlobalRoleService globalRoleService) {
        this.fakeDataService = fakeDataService;
        this.authService = authService;
        this.globalRoleService = globalRoleService;
    }

    @Unlocked
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/generateFakeData")
    @PermissionRequired(NamedPermission.MANUAL_VALUE_CHANGES)
    public void generateFakeData(@RequestParam final int users, @RequestParam final int projectsPerUser) {
        this.fakeDataService.generate(users, projectsPerUser);
    }

    @Unlocked
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/generateE2EData")
    @PermissionRequired(NamedPermission.MANUAL_VALUE_CHANGES)
    public void generateE2EData() {
        this.fakeDataService.generateE2EData();
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/createUser")
    public void createUser(@RequestBody final CreateUserRequest request) {
        this.config.checkDev();
        final UserTable userTable = authService.registerUserInternal(new SignupForm(request.username, request.email, request.password, true, null), true, true);
        if (request.admin) {
            globalRoleService.addRole(new GlobalRoleTable(userTable.getUserId(), GlobalRole.HANGAR_ADMIN));
        }
    }
    public record CreateUserRequest(String username, String password, String email, boolean admin) {}
}
