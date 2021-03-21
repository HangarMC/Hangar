package io.papermc.hangar.model.internal.api.requests;

import io.papermc.hangar.controller.validations.Validate;
import io.papermc.hangar.model.common.roles.OrganizationRole;

import java.util.List;

public class CreateOrganizationForm extends EditMembersForm<OrganizationRole> {

    @Validate(SpEL = "@validate.regex(#root, @hangarConfig.org.nameRegex)", message = "organizations.new.error.invalidName")
    @Validate(SpEL = "@validate.max(#root, @hangarConfig.org.maxNameLen)", message = "organizations.new.error.invalidName")
    @Validate(SpEL = "@validate.min(#root, @hangarConfig.org.minNameLen)", message = "organizations.new.error.invalidName")
    private final String name;

    public CreateOrganizationForm(List<Member<OrganizationRole>> members, String name) {
        super(members);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "CreateOrganizationForm{" +
                "name='" + name + '\'' +
                "} " + super.toString();
    }
}
