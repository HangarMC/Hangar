package io.papermc.hangar.security.metadatasources;

import io.papermc.hangar.security.PermissionAttribute;
import io.papermc.hangar.security.annotations.ProjectPermission;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.annotation.AnnotationMetadataExtractor;

import java.util.Collection;

public class ProjectPermissionSource implements AnnotationMetadataExtractor<ProjectPermission> {
    @Override
    public Collection<? extends ConfigAttribute> extractAttributes(ProjectPermission securityAnnotation) {
        return PermissionAttribute.createList(securityAnnotation.value());
    }
}
