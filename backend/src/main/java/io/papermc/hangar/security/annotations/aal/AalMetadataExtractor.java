package io.papermc.hangar.security.annotations.aal;

import java.util.Collection;
import java.util.Set;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.annotation.AnnotationMetadataExtractor;
import org.springframework.stereotype.Component;

@Component
public class AalMetadataExtractor implements AnnotationMetadataExtractor<RequireAal> {

    @Override
    public Collection<? extends ConfigAttribute> extractAttributes(final RequireAal annotation) {
        return Set.of(new AalAttribute(annotation.value()));
    }

    record AalAttribute(int aal) implements ConfigAttribute {

        @Override
        public String getAttribute() {
            return String.valueOf(this.aal);
        }
    }
}
