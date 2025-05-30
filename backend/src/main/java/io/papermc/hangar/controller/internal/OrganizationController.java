package io.papermc.hangar.controller.internal;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.components.images.service.AvatarService;
import io.papermc.hangar.db.customtypes.JSONB;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.common.NamedPermission;
import io.papermc.hangar.model.common.Permission;
import io.papermc.hangar.model.common.PermissionType;
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
import io.papermc.hangar.security.annotations.aal.RequireAal;
import io.papermc.hangar.security.annotations.permission.PermissionRequired;
import io.papermc.hangar.security.annotations.ratelimit.RateLimit;
import io.papermc.hangar.security.annotations.unlocked.Unlocked;
import io.papermc.hangar.security.authentication.HangarPrincipal;
import io.papermc.hangar.service.ValidationService;
import io.papermc.hangar.service.internal.organizations.OrganizationFactory;
import io.papermc.hangar.service.internal.organizations.OrganizationService;
import io.papermc.hangar.service.internal.perms.members.OrganizationMemberService;
import io.papermc.hangar.service.internal.users.UserService;
import io.papermc.hangar.service.internal.users.invites.OrganizationInviteService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;

// @el(org: io.papermc.hangar.model.db.OrganizationTable)
@RestController
@RateLimit(path = "organization")
@RequestMapping("/api/internal/organizations")
public class OrganizationController extends HangarComponent {

    private final UserService userService;
    private final OrganizationFactory organizationFactory;
    private final OrganizationService organizationService;
    private final OrganizationMemberService memberService;
    private final OrganizationInviteService inviteService;
    private final AvatarService avatarService;
    private final ValidationService validationService;

    @Autowired
    public OrganizationController(final UserService userService, final OrganizationFactory organizationFactory, final OrganizationService organizationService, final OrganizationMemberService memberService, final OrganizationInviteService inviteService, final AvatarService avatarService, final ValidationService validationService) {
        this.userService = userService;
        this.organizationFactory = organizationFactory;
        this.organizationService = organizationService;
        this.memberService = memberService;
        this.inviteService = inviteService;
        this.avatarService = avatarService;
        this.validationService = validationService;
    }

    @Anyone
    @ResponseStatus(HttpStatus.OK)
    @RateLimit(overdraft = 20, refillTokens = 10, refillSeconds = 1)
    @GetMapping("/validate")
    public void validateName(@RequestParam final String name) {
        if (!this.validationService.isValidOrgName(name)) {
            throw new HangarApiException("author.error.invalidUsername");
        }
        if (this.userService.getUserTable(name) != null) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "/org/{org}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<HangarOrganization> getOrganization(@PathVariable final OrganizationTable org) {
        return ResponseEntity.ok(this.organizationService.getHangarOrganization(org));
    }

    @Unlocked
    @RequireAal(1)
    @ResponseStatus(HttpStatus.OK)
    @RateLimit(overdraft = 7, refillTokens = 2, refillSeconds = 10)
    @PermissionRequired(type = PermissionType.ORGANIZATION, perms = NamedPermission.MANAGE_SUBJECT_MEMBERS, args = "{#org}")
    @PostMapping(path = "/org/{org}/members/add", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void addOrganizationMember(@PathVariable final OrganizationTable org, @RequestBody @Valid final EditMembersForm.OrgMember member) {
        this.inviteService.sendInvite(member, org);
    }

    @Unlocked
    @RequireAal(1)
    @ResponseStatus(HttpStatus.OK)
    @RateLimit(overdraft = 5, refillTokens = 2, refillSeconds = 10)
    @PermissionRequired(type = PermissionType.ORGANIZATION, perms = NamedPermission.MANAGE_SUBJECT_MEMBERS, args = "{#org}")
    @PostMapping(path = "/org/{org}/members/edit", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void editOrganizationMember(@PathVariable final OrganizationTable org, @RequestBody @Valid final EditMembersForm.OrgMember member) {
        this.memberService.editMember(member, org);
    }

    @Unlocked
    @RequireAal(1)
    @ResponseStatus(HttpStatus.OK)
    @RateLimit(overdraft = 7, refillTokens = 2, refillSeconds = 10)
    @PermissionRequired(type = PermissionType.ORGANIZATION, perms = NamedPermission.MANAGE_SUBJECT_MEMBERS, args = "{#org}")
    @PostMapping(path = "/org/{org}/members/remove", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void removeOrganizationMember(@PathVariable final OrganizationTable org, @RequestBody @Valid final EditMembersForm.OrgMember member) {
        this.memberService.removeMember(member, org);
    }

    @Unlocked
    @RequireAal(1)
    @ResponseStatus(HttpStatus.OK)
    @PermissionRequired(type = PermissionType.ORGANIZATION, perms = NamedPermission.IS_SUBJECT_MEMBER, args = "{#org}")
    @PostMapping(path = "/org/{org}/members/leave", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void leaveOrganization(@PathVariable final OrganizationTable org) {
        this.memberService.leave(org);
    }

    @Unlocked
    @RequireAal(1)
    @ResponseStatus(HttpStatus.OK)
    @PermissionRequired(type = PermissionType.ORGANIZATION, perms = NamedPermission.IS_SUBJECT_OWNER, args = "{#org}")
    @RateLimit(overdraft = 5, refillTokens = 1, refillSeconds = 60)
    @PostMapping(path = "/org/{org}/transfer", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void transferOrganization(@PathVariable final OrganizationTable org, @RequestBody @Valid final StringContent nameContent) {
        this.inviteService.sendTransferRequest(nameContent.getContent(), org);
    }

    @Unlocked
    @RequireAal(1)
    @ResponseStatus(HttpStatus.OK)
    @PermissionRequired(type = PermissionType.ORGANIZATION, perms = NamedPermission.IS_SUBJECT_OWNER, args = "{#org}")
    @PostMapping(path = "/org/{org}/canceltransfer", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void cancelOrganizationTransfer(@PathVariable final OrganizationTable org) {
        this.inviteService.cancelTransferRequest(org);
    }

    @Unlocked
    @RequireAal(1)
    @ResponseStatus(HttpStatus.OK)
    @RateLimit(overdraft = 3, refillTokens = 1, refillSeconds = 60)
    @PostMapping(path = "/create", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void create(@RequestBody @Valid final CreateOrganizationForm createOrganizationForm) {
        this.organizationFactory.createOrganization(createOrganizationForm.getName());
    }

    @Unlocked
    @RequireAal(1)
    @ResponseStatus(HttpStatus.OK)
    @RateLimit(overdraft = 3, refillTokens = 1, refillSeconds = 60)
    @PermissionRequired(type = PermissionType.ORGANIZATION, perms = NamedPermission.DELETE_ORGANIZATION, args = "{#org}")
    @PostMapping(path = "/org/{org}/delete", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void delete(@PathVariable final OrganizationTable org, @RequestBody @Valid final StringContent content) {
        this.organizationFactory.deleteOrganization(org, content.getContent());
    }

    @Unlocked
    @RequireAal(1)
    @ResponseStatus(HttpStatus.OK)
    @RateLimit(overdraft = 7, refillTokens = 1, refillSeconds = 20)
    @PermissionRequired(type = PermissionType.ORGANIZATION, perms = NamedPermission.EDIT_SUBJECT_SETTINGS, args = "{#org}")
    @PostMapping(path = "/org/{org}/settings/tagline", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void saveTagline(@PathVariable final OrganizationTable org, @RequestBody final StringContent content) {
        final String s = content.contentOrEmpty();
        if (s.length() > this.config.users().maxTaglineLen()) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "author.error.invalidTagline");
        }

        final UserTable userTable = this.userService.getUserTable(org.getUserId());
        if (userTable == null) {
            throw new HangarApiException(HttpStatus.NOT_FOUND);
        }

        final String oldTagline = userTable.getTagline() == null ? "" : userTable.getTagline();
        userTable.setTagline(s);
        this.userService.updateUser(userTable);
        this.actionLogger.user(LogAction.USER_TAGLINE_CHANGED.create(UserContext.of(userTable.getId()), userTable.getTagline(), oldTagline));
    }

    @Unlocked
    @RequireAal(1)
    @ResponseStatus(HttpStatus.OK)
    @RateLimit(overdraft = 7, refillTokens = 1, refillSeconds = 20)
    @PermissionRequired(type = PermissionType.ORGANIZATION, perms = NamedPermission.EDIT_SUBJECT_SETTINGS, args = "{#org}")
    @PostMapping(path = "/org/{org}/settings/socials", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void saveSocials(@PathVariable final OrganizationTable org, @RequestBody final Map<String, String> socials) {
        final UserTable userTable = this.userService.getUserTable(org.getUserId());
        if (userTable == null) {
            throw new HangarApiException(HttpStatus.NOT_FOUND);
        }

        this.userService.validateSocials(socials);

        final JSONB oldSocials = userTable.getSocials() == null ? new JSONB("[]") : userTable.getSocials();
        userTable.setSocials(new JSONB(socials));
        this.userService.updateUser(userTable);
        this.actionLogger.user(LogAction.USER_SOCIALS_CHANGED.create(UserContext.of(userTable.getId()), userTable.getSocials().toString(), oldSocials.toString()));
    }

    @Unlocked
    @RequireAal(1)
    @ResponseBody
    @RateLimit(overdraft = 5, refillTokens = 1, refillSeconds = 60)
    @PermissionRequired(type = PermissionType.ORGANIZATION, perms = NamedPermission.EDIT_SUBJECT_SETTINGS, args = "{#org}")
    @PostMapping(value = "/org/{org}/settings/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void changeAvatar(@PathVariable final OrganizationTable org, @RequestParam final MultipartFile avatar) throws IOException {
        final UserTable userTable = this.userService.getUserTable(org.getUserId());
        if (userTable == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown org user " + org.getName() + " (" + org.getUserId() + ")");
        }
        this.avatarService.changeUserAvatar(userTable, avatar.getBytes());
    }

    @Anyone
    @GetMapping(path = "/{user}/userOrganizations", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, OrganizationRoleTable>> getUserOrganizationRoles(@PathVariable final String user) {
        boolean includeHidden = false;
        final Optional<HangarPrincipal> principal = this.getOptionalHangarPrincipal();
        if (principal.isPresent()) {
            includeHidden = principal.get().getName().equals(user);
            if (!includeHidden) {
                includeHidden = principal.get().isAllowedGlobal(Permission.SeeHidden);
            }
        }
        return ResponseEntity.ok(this.organizationService.getUserOrganizationRoles(user, includeHidden));
    }

    @LoggedIn
    @GetMapping(path = "/{user}/userOrganizationsVisibility", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Boolean>> getUserOrganizationMembershipVisibility(@PathVariable final String user) {
        final HangarPrincipal principal = this.getHangarPrincipal();
        if (principal.getName().equals(user) || principal.isAllowedGlobal(Permission.SeeHidden)) {
            return ResponseEntity.ok(this.memberService.getUserOrganizationMembershipVisibility(user));
        }
        throw new HangarApiException(HttpStatus.BAD_REQUEST);
    }

    @Unlocked
    @PostMapping(path = "/{org}/userOrganizationsVisibility", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void changeUserOrganizationMembershipVisibility(@RequestParam final boolean hidden, @PathVariable final OrganizationTable org) {
        this.memberService.setMembershipVisibility(org.getOrganizationId(), this.getHangarPrincipal().getUserId(), hidden);
    }
}
