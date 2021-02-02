package io.papermc.hangar.securityold.metadatasources;

import io.papermc.hangar.securityold.annotations.OrganizationPermission;
import io.papermc.hangar.securityold.attributes.OrganizationPermissionAttribute;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.annotation.AnnotationMetadataExtractor;

import java.util.Collection;

public class OrganizationPermissionSource implements AnnotationMetadataExtractor<OrganizationPermission> {
    @Override
    public Collection<? extends ConfigAttribute> extractAttributes(OrganizationPermission securityAnnotation) {
        return OrganizationPermissionAttribute.createList(securityAnnotation.value());
    }
}
