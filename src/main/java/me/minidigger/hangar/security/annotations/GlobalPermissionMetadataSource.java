package me.minidigger.hangar.security.annotations;

import me.minidigger.hangar.security.attributes.PermissionAttribute;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.annotation.AnnotationMetadataExtractor;
import org.springframework.security.access.method.AbstractFallbackMethodSecurityMetadataSource;
import org.springframework.util.Assert;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;

public class GlobalPermissionMetadataSource extends AbstractFallbackMethodSecurityMetadataSource {

    private final AnnotationMetadataExtractor annotationExtractor;
    private Class<? extends Annotation> annotationType;

    public GlobalPermissionMetadataSource() {
        this(new GlobalPermissionAnnotationMetadataExtractor());
    }

    public GlobalPermissionMetadataSource(AnnotationMetadataExtractor annotationMetadataExtractor) {
        Assert.notNull(annotationMetadataExtractor, "annotationMetadataExtractor cannot be null");
        this.annotationExtractor = annotationMetadataExtractor;

        annotationType = (Class<? extends Annotation>) GenericTypeResolver.resolveTypeArgument(annotationExtractor.getClass(), AnnotationMetadataExtractor.class);
        Assert.notNull(annotationType, () -> annotationExtractor.getClass().getName() + " must supply a generic parameter for AnnotationMetadataExtractor");
    }

    @Override
    protected Collection<ConfigAttribute> findAttributes(Method method, Class<?> targetClass) {
        return processAnnotation(AnnotationUtils.findAnnotation(method, annotationType));
    }

    @Override
    protected Collection<ConfigAttribute> findAttributes(Class<?> clazz) {

        return processAnnotation(AnnotationUtils.findAnnotation(clazz, annotationType));
    }

    @Override
    public Collection<ConfigAttribute> getAllConfigAttributes() {
        return null;
    }

    private Collection<ConfigAttribute> processAnnotation(Annotation a) {
        if (a == null) {
            return null;
        }

        return annotationExtractor.extractAttributes(a);
    }
}

class GlobalPermissionAnnotationMetadataExtractor implements AnnotationMetadataExtractor<GlobalPermission> {
    @Override
    public Collection<? extends ConfigAttribute> extractAttributes(GlobalPermission securityAnnotation) {
        return PermissionAttribute.createList(securityAnnotation.value());
    }
}
