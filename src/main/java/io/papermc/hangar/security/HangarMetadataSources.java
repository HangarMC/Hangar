package io.papermc.hangar.security;

import org.jetbrains.annotations.NotNull;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.annotation.AnnotationMetadataExtractor;
import org.springframework.security.access.method.AbstractFallbackMethodSecurityMetadataSource;
import org.springframework.util.Assert;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@SuppressWarnings("rawtypes")
public class HangarMetadataSources extends AbstractFallbackMethodSecurityMetadataSource {

    private final Map<Class<? extends Annotation>, AnnotationMetadataExtractor> annotationExtractors = new HashMap<>();

    public HangarMetadataSources(AnnotationMetadataExtractor...annotationMetadataExtractors) {
        this(Arrays.asList(annotationMetadataExtractors));
    }

    @SuppressWarnings("unchecked")
    public HangarMetadataSources(Collection<AnnotationMetadataExtractor> annotationMetadataExtractors) {
        Assert.notEmpty(annotationMetadataExtractors, "Must add an AnnotationMetadatExtractor");

        annotationMetadataExtractors.forEach(annotationMetadataExtractor -> {
            Class<? extends Annotation> annotationType = (Class<? extends Annotation>) GenericTypeResolver.resolveTypeArgument(annotationMetadataExtractor.getClass(), AnnotationMetadataExtractor.class);
            Assert.notNull(annotationType, () -> annotationMetadataExtractor.getClass().getName() + " must supply a generic parameter for AnnotationMetadataExtractor");
            annotationExtractors.put(annotationType, annotationMetadataExtractor);
        });
    }

    @Override
    protected Collection<ConfigAttribute> findAttributes(Method method, Class<?> targetClass) {
        return Stream.concat(
                annotationExtractors.entrySet().stream()
                        .map(entry -> processAnnotation(method, entry))
                        .flatMap(Collection::stream),
                annotationExtractors.entrySet().stream()
                        .map(entry -> processAnnotation(targetClass, entry))
                        .flatMap(Collection::stream)
        ).collect(Collectors.toUnmodifiableSet());
    }

    @Override
    protected Collection<ConfigAttribute> findAttributes(Class<?> clazz) {
        return annotationExtractors.entrySet().stream().map(entry -> processAnnotation(clazz, entry)).flatMap(Collection::stream).collect(Collectors.toList());
    }

    @Override
    public Collection<ConfigAttribute> getAllConfigAttributes() {
        return null;
    }

    @SuppressWarnings("unchecked")
    @NotNull
    private Collection<ConfigAttribute> processAnnotation(AnnotatedElement element, Map.Entry<Class<? extends Annotation>, AnnotationMetadataExtractor> entry) {
        MergedAnnotations annotations = MergedAnnotations.from(element);
        return (Collection<ConfigAttribute>) annotations.stream(entry.getKey()).map(ma -> entry.getValue().extractAttributes(ma.synthesize())).flatMap(Collection::stream).collect(Collectors.toList());
    }
}

