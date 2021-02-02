package io.papermc.hangar.securityold.metadatasources;

import io.papermc.hangar.securityold.annotations.ProjectPermission;
import io.papermc.hangar.securityold.attributes.ProjectPermissionAttribute;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.annotation.AnnotationMetadataExtractor;

import java.util.Collection;

public class ProjectPermissionSource implements AnnotationMetadataExtractor<ProjectPermission> {
    @Override
    public Collection<? extends ConfigAttribute> extractAttributes(ProjectPermission securityAnnotation) {
        return ProjectPermissionAttribute.createList(securityAnnotation.value());
    }
}
