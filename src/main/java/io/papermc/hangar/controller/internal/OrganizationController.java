package io.papermc.hangar.controller.internal;

import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.internal.api.requests.CreateOrganizationForm;
import io.papermc.hangar.security.annotations.Anyone;
import io.papermc.hangar.security.annotations.unlocked.Unlocked;
import io.papermc.hangar.service.internal.organizations.OrganizationFactory;
import io.papermc.hangar.service.internal.users.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.validation.Valid;

@Controller
@RequestMapping("/api/internal/organizations")
public class OrganizationController {

    private final UserService userService;
    private final OrganizationFactory organizationFactory;

    @Autowired
    public OrganizationController(UserService userService, OrganizationFactory organizationFactory) {
        this.userService = userService;
        this.organizationFactory = organizationFactory;
    }

    @Anyone
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/validate")
    public void validateName(@RequestParam String name) {
        if (userService.getUserTable(name) != null) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST);
        }
    }

    @Unlocked
    @ResponseStatus(HttpStatus.OK)
    @PostMapping(path = "/create", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void create(@Valid @RequestBody CreateOrganizationForm createOrganizationForm) {
        System.out.println(createOrganizationForm);
        organizationFactory.createOrganization(createOrganizationForm.getName(), createOrganizationForm.getNewMembers());
    }
}
