package io.papermc.hangar.controller.internal;

import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.db.roles.OrganizationRoleTable;
import io.papermc.hangar.model.internal.HangarOrganization;
import io.papermc.hangar.model.internal.api.requests.CreateOrganizationForm;
import io.papermc.hangar.security.annotations.Anyone;
import io.papermc.hangar.security.annotations.unlocked.Unlocked;
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
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.validation.Valid;
import java.util.Map;

@Controller
@RequestMapping("/api/internal/organizations")
public class OrganizationController {

    private final UserService userService;
    private final OrganizationFactory organizationFactory;
    private final OrganizationService organizationService;

    @Autowired
    public OrganizationController(UserService userService, OrganizationFactory organizationFactory, OrganizationService organizationService) {
        this.userService = userService;
        this.organizationFactory = organizationFactory;
        this.organizationService = organizationService;
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
    @PostMapping(path = "/create", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void create(@Valid @RequestBody CreateOrganizationForm createOrganizationForm) {
        System.out.println(createOrganizationForm);
        organizationFactory.createOrganization(createOrganizationForm.getName(), createOrganizationForm.getNewMembers());
    }

    @Anyone
    @GetMapping(path = "/{user}/userOrganizations", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, OrganizationRoleTable>> getUserOrganizationRoles(@PathVariable String user) {
        return ResponseEntity.ok(organizationService.getUserOrganizationRoles(user));
    }
}
