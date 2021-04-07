package io.papermc.hangar.controller.internal;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.common.NamedPermission;
import io.papermc.hangar.model.common.PermissionType;
import io.papermc.hangar.model.common.roles.OrganizationRole;
import io.papermc.hangar.model.db.OrganizationTable;
import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.model.db.roles.OrganizationRoleTable;
import io.papermc.hangar.model.internal.HangarOrganization;
import io.papermc.hangar.model.internal.api.requests.CreateOrganizationForm;
import io.papermc.hangar.model.internal.api.requests.EditMembersForm;
import io.papermc.hangar.model.internal.api.requests.StringContent;
import io.papermc.hangar.model.internal.logs.LogAction;
import io.papermc.hangar.model.internal.logs.contexts.UserContext;
import io.papermc.hangar.security.annotations.Anyone;
import io.papermc.hangar.security.annotations.permission.PermissionRequired;
import io.papermc.hangar.security.annotations.unlocked.Unlocked;
import io.papermc.hangar.service.AuthenticationService;
import io.papermc.hangar.service.internal.organizations.OrganizationFactory;
import io.papermc.hangar.service.internal.organizations.OrganizationService;
import io.papermc.hangar.service.internal.users.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.validation.Valid;
import java.net.URI;
import java.util.Map;

@Controller
@RequestMapping("/api/internal/organizations")
public class OrganizationController extends HangarComponent {

    private final UserService userService;
    private final OrganizationFactory organizationFactory;
    private final OrganizationService organizationService;
    private final AuthenticationService authenticationService;

    @Autowired
    public OrganizationController(UserService userService, OrganizationFactory organizationFactory, OrganizationService organizationService, AuthenticationService authenticationService) {
        this.userService = userService;
        this.organizationFactory = organizationFactory;
        this.organizationService = organizationService;
        this.authenticationService = authenticationService;
    }

    @Anyone
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/validate")
    public void validateName(@RequestParam String name) {
        if (userService.getUserTable(name) != null) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "/org/{name}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<HangarOrganization> getOrganization(@PathVariable String name) {
        return ResponseEntity.ok(organizationService.getHangarOrganization(name));
    }

    @Unlocked
    @ResponseStatus(HttpStatus.OK)
    @PermissionRequired(type = PermissionType.ORGANIZATION, perms = NamedPermission.EDIT_SUBJECT_SETTINGS, args = "{#name}")
    @PostMapping(path = "/org/{name}/members", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void editProjectMembers(@PathVariable String name, @Valid @RequestBody EditMembersForm<OrganizationRole> editMembersForm) {
        organizationService.editMembers(name, editMembersForm);
    }

    @Unlocked
    @ResponseStatus(HttpStatus.OK)
    @PostMapping(path = "/create", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void create(@Valid @RequestBody CreateOrganizationForm createOrganizationForm) {
        organizationFactory.createOrganization(createOrganizationForm.getName(), createOrganizationForm.getNewInvitees());
    }

    @Unlocked
    @ResponseStatus(HttpStatus.OK)
    @PermissionRequired(type = PermissionType.ORGANIZATION, perms = NamedPermission.EDIT_SUBJECT_SETTINGS, args = "{#name}")
    @PostMapping(path = "/org/{name}/settings/tagline", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void saveTagline(@PathVariable String name, @Valid @RequestBody StringContent content) {
        UserTable userTable = userService.getUserTable(name);
        OrganizationTable organizationTable = organizationService.getOrganizationTable(name);
        if (userTable == null || organizationTable == null) {
            throw new HangarApiException(HttpStatus.NOT_FOUND);
        }
        if (content.getContent().length() > config.user.getMaxTaglineLen()) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "author.error.invalidTagline");
        }
        String oldTagline = userTable.getTagline() == null ? "" : userTable.getTagline();
        userTable.setTagline(content.getContent());
        userService.updateUser(userTable);
        userActionLogService.user(LogAction.USER_TAGLINE_CHANGED.create(UserContext.of(userTable.getId()), userTable.getTagline(), oldTagline));
    }

    @Unlocked
    @ResponseBody
    @PermissionRequired(type = PermissionType.ORGANIZATION, perms = NamedPermission.EDIT_SUBJECT_SETTINGS, args = "{#name}")
    @GetMapping(value = "/org/{name}/settings/avatar", produces = MediaType.TEXT_PLAIN_VALUE)
    public String getUpdateAvatarUri(@PathVariable String name) {
        try {
            URI uri = authenticationService.changeAvatarUri(getHangarPrincipal().getName(), name);
            return uri.toString();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new HangarApiException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Anyone
    @GetMapping(path = "/{user}/userOrganizations", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, OrganizationRoleTable>> getUserOrganizationRoles(@PathVariable String user) {
        return ResponseEntity.ok(organizationService.getUserOrganizationRoles(user));
    }
}
