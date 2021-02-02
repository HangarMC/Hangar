package io.papermc.hangar.securityold.metadatasources;

import io.papermc.hangar.securityold.annotations.GlobalPermission;
import io.papermc.hangar.securityold.attributes.GlobalPermissionAttribute;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.annotation.AnnotationMetadataExtractor;

import java.util.Collection;

public class GlobalPermissionSource implements AnnotationMetadataExtractor<GlobalPermission> {
    @Override
    public Collection<? extends ConfigAttribute> extractAttributes(GlobalPermission securityAnnotation) {
        return GlobalPermissionAttribute.createList(securityAnnotation.value());
    }
}
