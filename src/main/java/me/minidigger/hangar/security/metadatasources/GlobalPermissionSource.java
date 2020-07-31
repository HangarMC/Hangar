package me.minidigger.hangar.security.metadatasources;

import me.minidigger.hangar.security.PermissionAttribute;
import me.minidigger.hangar.security.annotations.GlobalPermission;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.annotation.AnnotationMetadataExtractor;

import java.util.Collection;

public class GlobalPermissionSource implements AnnotationMetadataExtractor<GlobalPermission> {
    @Override
    public Collection<? extends ConfigAttribute> extractAttributes(GlobalPermission securityAnnotation) {
        return PermissionAttribute.createList(securityAnnotation.value());
    }
}
