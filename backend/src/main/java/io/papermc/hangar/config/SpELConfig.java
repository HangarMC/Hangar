package io.papermc.hangar.config;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.expression.spel.support.StandardEvaluationContext;

@Configuration
public class SpELConfig {

    @Bean
    public StandardEvaluationContext standardEvaluationContext(final ApplicationContext applicationContext) {
        final StandardEvaluationContext evaluationContext = new StandardEvaluationContext();
        evaluationContext.setBeanResolver(new BeanFactoryResolver(applicationContext));
        return evaluationContext;
    }
}
