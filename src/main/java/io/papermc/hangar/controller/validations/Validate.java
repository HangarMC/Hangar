package io.papermc.hangar.controller.validations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = Validate.ValidateConstraintValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(Validate.List.class)
public @interface Validate {
    String SpEL() default "";
    String message() default "Invalid field";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    class ValidateConstraintValidator implements ConstraintValidator<Validate, Object> {

        @Autowired
        StandardEvaluationContext evaluationContext;
        private Expression expression;

        @Override
        public void initialize(Validate constraintAnnotation) {
            ExpressionParser expressionParser = new SpelExpressionParser();
            expression = expressionParser.parseExpression(constraintAnnotation.SpEL());
        }

        @Override
        public boolean isValid(Object value, ConstraintValidatorContext context) {
            if (value == null) return false;
            Boolean bool = expression.getValue(evaluationContext, value, boolean.class);
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
