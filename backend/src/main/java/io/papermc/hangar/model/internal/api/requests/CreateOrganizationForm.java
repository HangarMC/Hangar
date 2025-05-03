package io.papermc.hangar.model.internal.api.requests;

import io.papermc.hangar.controller.validations.Validate;
import java.util.List;

public class CreateOrganizationForm extends EditMembersForm.EditOrgMembersForm {
    // @el(root: String)
    @Validate(SpEL = "@validate.required(#root)", message = "organization.new.error.invalidName")
    @Validate(SpEL = "@validate.regex(#root, @'hangar-io.papermc.hangar.config.hangar.HangarConfig'.orgs.nameRegex)", message = "organization.new.error.invalidName")
    @Validate(SpEL = "@validate.max(#root, @'hangar-io.papermc.hangar.config.hangar.HangarConfig'.orgs.maxNameLen)", message = "organization.new.error.invalidName")
    @Validate(SpEL = "@validate.min(#root, @'hangar-io.papermc.hangar.config.hangar.HangarConfig'.orgs.minNameLen)", message = "organization.new.error.invalidName")
    private final String name;

    public CreateOrganizationForm(final List<OrgMember> members, final String name) {
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
