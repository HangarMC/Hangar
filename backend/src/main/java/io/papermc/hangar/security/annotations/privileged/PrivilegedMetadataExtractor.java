package io.papermc.hangar.security.annotations.privileged;

import java.util.Collection;
import java.util.Set;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.annotation.AnnotationMetadataExtractor;
import org.springframework.stereotype.Component;

@Component
public class PrivilegedMetadataExtractor implements AnnotationMetadataExtractor<Privileged> {

    @Override
    public Collection<? extends ConfigAttribute> extractAttributes(final Privileged securityAnnotation) {
        return Set.of(PrivilegedUnlockedAttribute.INSTANCE);
    }

    static final class PrivilegedUnlockedAttribute implements ConfigAttribute {

        static final PrivilegedUnlockedAttribute INSTANCE = new PrivilegedUnlockedAttribute();

        private PrivilegedUnlockedAttribute() {
        }

        @Override
        public String getAttribute() {
            return "PRIVILEGED";
        }
    }
}
