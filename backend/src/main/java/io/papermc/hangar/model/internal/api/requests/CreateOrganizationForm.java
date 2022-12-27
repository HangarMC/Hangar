package io.papermc.hangar.model.internal.api.requests;

import io.papermc.hangar.controller.validations.Validate;
import io.papermc.hangar.model.common.roles.OrganizationRole;
import java.util.List;

public class CreateOrganizationForm extends EditMembersForm<OrganizationRole> {
    // @el(root: String)
    private final @Validate(SpEL = "@validate.required(#root)", message = "organization.new.error.invalidName") @Validate(SpEL = "@validate.regex(#root, @hangarConfig.org.nameRegex)", message = "organization.new.error.invalidName") @Validate(SpEL = "@validate.max(#root, @hangarConfig.org.maxNameLen)", message = "organization.new.error.invalidName") @Validate(SpEL = "@validate.min(#root, @hangarConfig.org.minNameLen)", message = "organization.new.error.invalidName") String name;

    public CreateOrganizationForm(final List<Member<OrganizationRole>> members, final String name) {
        super(members);
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        return "CreateOrganizationForm{" +
            "name='" + this.name + '\'' +
            "} " + super.toString();
    }
}
