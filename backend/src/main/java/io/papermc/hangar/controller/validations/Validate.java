package io.papermc.hangar.controller.validations;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import org.intellij.lang.annotations.Language;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = Validate.ValidateConstraintValidator.class)
@Target({ElementType.FIELD, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(Validate.List.class)
public @interface Validate {
    @Language("SpEL")
    String SpEL() default "";

    String message() default "Invalid field";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class ValidateConstraintValidator implements ConstraintValidator<Validate, Object> {

        @Autowired
        StandardEvaluationContext evaluationContext;
        private Expression expression;

        @Override
        public void initialize(final Validate constraintAnnotation) {
            final ExpressionParser expressionParser = new SpelExpressionParser();
            this.expression = expressionParser.parseExpression(constraintAnnotation.SpEL());
        }

        @Override
        public boolean isValid(final Object value, final ConstraintValidatorContext context) {
            final Boolean bool = this.expression.getValue(this.evaluationContext, value, boolean.class);
            return bool != null && bool;
        }
    }

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @interface List {
        Validate[] value();
    }
}
