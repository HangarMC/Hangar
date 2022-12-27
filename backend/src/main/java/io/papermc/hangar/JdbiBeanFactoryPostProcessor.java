package io.papermc.hangar;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Repository;

// https://stackoverflow.com/questions/61526870/spring-boot-custom-bean-loader
public class JdbiBeanFactoryPostProcessor implements BeanDefinitionRegistryPostProcessor, ResourceLoaderAware, EnvironmentAware, BeanClassLoaderAware, BeanFactoryAware {

    private BeanFactory beanFactory;
    private ResourceLoader resourceLoader;
    private Environment environment;
    private ClassLoader classLoader;

    @Override
    public void setResourceLoader(final @NotNull ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void setEnvironment(final @NotNull Environment environment) {
        this.environment = environment;
    }

    @Override
    public void setBeanClassLoader(final @NotNull ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public void setBeanFactory(final @NotNull BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override
    public void postProcessBeanFactory(final @NotNull ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {
        // not needed
    }

    @Override
    public void postProcessBeanDefinitionRegistry(final @NotNull BeanDefinitionRegistry registry) throws BeansException {
        final ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false) {
            @Override
            protected boolean isCandidateComponent(final @NotNull AnnotatedBeanDefinition beanDefinition) {
                // By default, scanner does not accept regular interface without @Lookup method, bypass this
                return true;
            }
        };
        scanner.setEnvironment(this.environment);
        scanner.setResourceLoader(this.resourceLoader);
        scanner.addIncludeFilter(new AnnotationTypeFilter(Repository.class));
        final List<String> basePackages = AutoConfigurationPackages.get(this.beanFactory);
        basePackages.stream()
            .map(scanner::findCandidateComponents)
            .flatMap(Collection::stream)
            .forEach(bd -> this.registerJdbiDaoBeanFactory(registry, bd));
    }

    private void registerJdbiDaoBeanFactory(final BeanDefinitionRegistry registry, final BeanDefinition bd) {
        final GenericBeanDefinition beanDefinition = (GenericBeanDefinition) bd;
        final Class<?> jdbiDaoClass;
        try {
            jdbiDaoClass = beanDefinition.resolveBeanClass(this.classLoader);
        } catch (final ClassNotFoundException e) {
            throw new FatalBeanException(beanDefinition.getBeanClassName() + " not found on classpath", e);
        }
        beanDefinition.setBeanClass(JdbiDaoBeanFactory.class);
        // Add dependency to your `Jdbi` bean by name
        beanDefinition.getConstructorArgumentValues().addGenericArgumentValue(new RuntimeBeanReference("jdbi"));
        beanDefinition.getConstructorArgumentValues().addGenericArgumentValue(Objects.requireNonNull(jdbiDaoClass));

        registry.registerBeanDefinition(jdbiDaoClass.getSimpleName(), beanDefinition);
    }
}

