package io.papermc.hangar.controller.extras.resolvers.path.model;

import io.papermc.hangar.model.db.OrganizationTable;
import io.papermc.hangar.service.internal.organizations.OrganizationService;
import io.papermc.hangar.util.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.NativeWebRequest;

@Component
public class OrganizationTableResolver extends HangarModelResolver<OrganizationTable> {

    private final OrganizationService organizationService;

    @Autowired
    public OrganizationTableResolver(final OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    @Override
    protected Class<OrganizationTable> modelType() {
        return OrganizationTable.class;
    }

    @Override
    protected OrganizationTable resolveParameter(final @NotNull String param, final NativeWebRequest request) {
        OrganizationTable organizationTable = null;
        if (this.supportsId(request) && StringUtils.isLong(param)) {
            organizationTable = this.organizationService.getOrganizationTable(Long.parseLong(param));
        }

        if (organizationTable == null) {
            organizationTable = this.organizationService.getOrganizationTable(param);
        }

        return organizationTable;
    }
}
