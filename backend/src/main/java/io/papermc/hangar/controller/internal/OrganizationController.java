package io.papermc.hangar.controller.internal;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.common.NamedPermission;
import io.papermc.hangar.model.common.Permission;
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
import io.papermc.hangar.security.annotations.LoggedIn;
import io.papermc.hangar.security.annotations.permission.PermissionRequired;
import io.papermc.hangar.security.annotations.ratelimit.RateLimit;
import io.papermc.hangar.security.annotations.unlocked.Unlocked;
import io.papermc.hangar.security.authentication.HangarPrincipal;
import io.papermc.hangar.service.AuthenticationService;
import io.papermc.hangar.service.ValidationService;
import io.papermc.hangar.service.internal.organizations.OrganizationFactory;
import io.papermc.hangar.service.internal.organizations.OrganizationService;
import io.papermc.hangar.service.internal.perms.members.OrganizationMemberService;
import io.papermc.hangar.service.internal.users.UserService;
import io.papermc.hangar.service.internal.users.invites.OrganizationInviteService;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import javax.validation.Valid;
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
import org.springframework.web.multipart.MultipartFile;

@Controller
@RateLimit(path = "organization")
@RequestMapping("/api/internal/organizations")
public class OrganizationController extends HangarComponent {

    private final UserService userService;
    private final OrganizationFactory organizationFactory;
    private final OrganizationService organizationService;
    private final OrganizationMemberService memberService;
    private final OrganizationInviteService inviteService;
    private final AuthenticationService authenticationService;
    private final ValidationService validationService;

    @Autowired
    public OrganizationController(UserService userService, OrganizationFactory organizationFactory, OrganizationService organizationService, OrganizationMemberService memberService, OrganizationInviteService inviteService, AuthenticationService authenticationService, ValidationService validationService) {
        this.userService = userService;
        this.organizationFactory = organizationFactory;
        this.organizationService = organizationService;
        this.memberService = memberService;
        this.inviteService = inviteService;
        this.authenticationService = authenticationService;
        this.validationService = validationService;
    }

    @Anyone
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/validate")
    public void validateName(@RequestParam String name) {
        if (!validationService.isValidUsername(name)) {
            throw new HangarApiException("author.error.invalidUsername");
        }
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
    @RateLimit(overdraft = 7, refillTokens = 2, refillSeconds = 10)
    @PermissionRequired(type = PermissionType.ORGANIZATION, perms = NamedPermission.MANAGE_SUBJECT_MEMBERS, args = "{#name}")
    @PostMapping(path = "/org/{name}/members/add", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void addOrganizationMember(@PathVariable String name, @Valid @RequestBody EditMembersForm.Member<OrganizationRole> member) {
        OrganizationTable organizationTable = organizationService.getOrganizationTable(name);
        if (organizationTable == null) {
            throw new HangarApiException("Org " + name + " doesn't exist");
        }
        inviteService.sendInvite(member, organizationTable);
    }

    @Unlocked
    @ResponseStatus(HttpStatus.OK)
    @RateLimit(overdraft = 5, refillTokens = 2, refillSeconds = 10)
    @PermissionRequired(type = PermissionType.ORGANIZATION, perms = NamedPermission.MANAGE_SUBJECT_MEMBERS, args = "{#name}")
    @PostMapping(path = "/org/{name}/members/edit", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void editOrganizationMember(@PathVariable String name, @Valid @RequestBody EditMembersForm.Member<OrganizationRole> member) {
        OrganizationTable organizationTable = organizationService.getOrganizationTable(name);
        memberService.editMember(member, organizationTable);
    }

    @Unlocked
    @ResponseStatus(HttpStatus.OK)
    @RateLimit(overdraft = 7, refillTokens = 2, refillSeconds = 10)
    @PermissionRequired(type = PermissionType.ORGANIZATION, perms = NamedPermission.MANAGE_SUBJECT_MEMBERS, args = "{#name}")
    @PostMapping(path = "/org/{name}/members/remove", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void removeOrganizationMember(@PathVariable String name, @Valid @RequestBody EditMembersForm.Member<OrganizationRole> member) {
        OrganizationTable organizationTable = organizationService.getOrganizationTable(name);
        memberService.removeMember(member, organizationTable);
    }

    @Unlocked
    @ResponseStatus(HttpStatus.OK)
    @PermissionRequired(type = PermissionType.ORGANIZATION, perms = NamedPermission.IS_SUBJECT_MEMBER, args = "{#name}")
    @PostMapping(path = "/org/{name}/members/leave", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void leaveOrganization(@PathVariable String name) {
        OrganizationTable organizationTable = organizationService.getOrganizationTable(name);
        memberService.leave(organizationTable);
    }

    @Unlocked
    @ResponseStatus(HttpStatus.OK)
    @PermissionRequired(type = PermissionType.ORGANIZATION, perms = NamedPermission.IS_SUBJECT_OWNER, args = "{#name}")
    @RateLimit(overdraft = 5, refillTokens = 1, refillSeconds = 60)
    @PostMapping(path = "/org/{name}/transfer", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void transferOrganization(@PathVariable String name, @Valid @RequestBody StringContent nameContent) {
        final OrganizationTable organizationTable = organizationService.getOrganizationTable(name);
        inviteService.sendTransferRequest(nameContent.getContent(), organizationTable);
    }

    @Unlocked
    @ResponseStatus(HttpStatus.OK)
    @PermissionRequired(type = PermissionType.ORGANIZATION, perms = NamedPermission.IS_SUBJECT_OWNER, args = "{#name}")
    @PostMapping(path = "/org/{name}/canceltransfer", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void cancelOrganizationTransfer(@PathVariable String name) {
        final OrganizationTable organizationTable = organizationService.getOrganizationTable(name);
        inviteService.cancelTransferRequest(organizationTable);
    }

    @Unlocked
    @ResponseStatus(HttpStatus.OK)
    @RateLimit(overdraft = 3, refillTokens = 1, refillSeconds = 60)
    @PostMapping(path = "/create", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void create(@Valid @RequestBody CreateOrganizationForm createOrganizationForm) {
        organizationFactory.createOrganization(createOrganizationForm.getName());
    }

    @Unlocked
    @ResponseStatus(HttpStatus.OK)
    @RateLimit(overdraft = 3, refillTokens = 1, refillSeconds = 60)
    @PermissionRequired(type = PermissionType.ORGANIZATION, perms = NamedPermission.DELETE_ORGANIZATION, args = "{#name}")
    @PostMapping(path = "/org/{name}/delete", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void delete(@PathVariable String name, @RequestBody @Valid StringContent content) {
        final OrganizationTable organizationTable = organizationService.getOrganizationTable(name);
        organizationFactory.deleteOrganization(organizationTable, content.getContent());
    }

    @Unlocked
    @ResponseStatus(HttpStatus.OK)
    @RateLimit(overdraft = 7, refillTokens = 1, refillSeconds = 20)
    @PermissionRequired(type = PermissionType.ORGANIZATION, perms = NamedPermission.EDIT_SUBJECT_SETTINGS, args = "{#name}")
    @PostMapping(path = "/org/{name}/settings/tagline", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void saveTagline(@PathVariable String name, @Valid @RequestBody StringContent content) {
        UserTable userTable = userService.getUserTable(name);
        OrganizationTable organizationTable = organizationService.getOrganizationTable(name);
        if (userTable == null || organizationTable == null) {
            throw new HangarApiException(HttpStatus.NOT_FOUND);
        }
        if (content.getContent().length() > config.user.maxTaglineLen()) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "author.error.invalidTagline");
        }
        String oldTagline = userTable.getTagline() == null ? "" : userTable.getTagline();
        userTable.setTagline(content.getContent());
        userService.updateUser(userTable);
        actionLogger.user(LogAction.USER_TAGLINE_CHANGED.create(UserContext.of(userTable.getId()), userTable.getTagline(), oldTagline));
    }

    @Unlocked
    @ResponseBody
    @RateLimit(overdraft = 5, refillTokens = 1, refillSeconds = 60)
    @PermissionRequired(type = PermissionType.ORGANIZATION, perms = NamedPermission.EDIT_SUBJECT_SETTINGS, args = "{#name}")
    @PostMapping(value = "/org/{name}/settings/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void changeAvatar(@PathVariable String name, @RequestParam MultipartFile avatar) throws IOException {
        authenticationService.changeAvatar(name, avatar);
    }

    @Anyone
    @GetMapping(path = "/{user}/userOrganizations", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, OrganizationRoleTable>> getUserOrganizationRoles(@PathVariable String user) {
        boolean includeHidden = false;
        Optional<HangarPrincipal> principal = getOptionalHangarPrincipal();
        if (principal.isPresent()) {
            includeHidden = principal.get().getName().equals(user);
            if (!includeHidden) {
                includeHidden = principal.get().isAllowedGlobal(Permission.SeeHidden);
            }
        }
        return ResponseEntity.ok(organizationService.getUserOrganizationRoles(user, includeHidden));
    }

    @LoggedIn
    @GetMapping(path = "/{user}/userOrganizationsVisibility", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Boolean>> getUserOrganizationMembershipVisibility(@PathVariable String user) {
        HangarPrincipal principal = getHangarPrincipal();
        if (principal.getName().equals(user) || principal.isAllowedGlobal(Permission.SeeHidden)) {
            return ResponseEntity.ok(memberService.getUserOrganizationMembershipVisibility(user));
        }
        throw new HangarApiException(HttpStatus.BAD_REQUEST);
    }

    @Unlocked
    @PostMapping(path = "/{org}/userOrganizationsVisibility", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void changeUserOrganizationMembershipVisibility(@RequestBody boolean hidden, @PathVariable String org) {
        OrganizationTable table = organizationService.getOrganizationTable(org);
        if (table == null) {
            throw new HangarApiException();
        }
        memberService.setMembershipVisibility(table.getOrganizationId(), getHangarPrincipal().getUserId(), hidden);
    }
}
