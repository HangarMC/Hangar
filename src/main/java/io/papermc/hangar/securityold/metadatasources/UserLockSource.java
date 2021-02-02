package io.papermc.hangar.securityold.metadatasources;

import io.papermc.hangar.securityold.annotations.UserLock;
import io.papermc.hangar.securityold.attributes.UserLockAttribute;
import io.papermc.hangar.util.Routes;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.annotation.AnnotationMetadataExtractor;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class UserLockSource implements AnnotationMetadataExtractor<UserLock> {
    @Override
    public Collection<? extends ConfigAttribute> extractAttributes(UserLock securityAnnotation) {
        Set<UserLockAttribute> attributes = new HashSet<>();
        Routes route = securityAnnotation.value();
        if (securityAnnotation.route() != route && securityAnnotation.route() != Routes.SHOW_HOME) {
            route = securityAnnotation.route();
        }
        attributes.add(new UserLockAttribute(
            route,
                securityAnnotation.args()
        ));
        return attributes;
    }
}
