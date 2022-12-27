package io.papermc.hangar.security.annotations.unlocked;

import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.annotation.AnnotationMetadataExtractor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Set;

@Component
public class UnlockedMetadataExtractor implements AnnotationMetadataExtractor<Unlocked> {

    @Override
    public Collection<? extends ConfigAttribute> extractAttributes(final Unlocked securityAnnotation) {
        return Set.of(UnlockedAttribute.INSTANCE);
    }

    static class UnlockedAttribute implements ConfigAttribute {

        static final UnlockedAttribute INSTANCE = new UnlockedAttribute();

        private UnlockedAttribute() { }

        @Override
        public String getAttribute() {
            return "UNLOCKED";
        }
    }
}
