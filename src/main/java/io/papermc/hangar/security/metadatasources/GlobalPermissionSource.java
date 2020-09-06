package io.papermc.hangar.security.metadatasources;

import io.papermc.hangar.security.annotations.GlobalPermission;
import io.papermc.hangar.security.attributes.GlobalPermissionAttribute;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.annotation.AnnotationMetadataExtractor;

import java.util.Collection;

public class GlobalPermissionSource implements AnnotationMetadataExtractor<GlobalPermission> {
    @Override
    public Collection<? extends ConfigAttribute> extractAttributes(GlobalPermission securityAnnotation) {
        return GlobalPermissionAttribute.createList(securityAnnotation.value());
    }
}
