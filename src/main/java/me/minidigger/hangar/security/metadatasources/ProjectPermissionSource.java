package me.minidigger.hangar.security.metadatasources;

import me.minidigger.hangar.security.PermissionAttribute;
import me.minidigger.hangar.security.annotations.ProjectPermission;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.annotation.AnnotationMetadataExtractor;

import java.util.Collection;

public class ProjectPermissionSource implements AnnotationMetadataExtractor<ProjectPermission> {
    @Override
    public Collection<? extends ConfigAttribute> extractAttributes(ProjectPermission securityAnnotation) {
        return PermissionAttribute.createList(securityAnnotation.value());
    }
}
