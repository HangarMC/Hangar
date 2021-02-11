package io.papermc.hangar.controller.validations;

import io.papermc.hangar.controller.validations.AtLeastOneNotNull.AtLeastOneNotNullValidator;
import org.springframework.beans.BeanUtils;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AtLeastOneNotNullValidator.class)
@Documented
public @interface AtLeastOneNotNull {
    String[] fieldNames();
    String message() default "Must have one non null field";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    class AtLeastOneNotNullValidator implements ConstraintValidator<AtLeastOneNotNull, Object> {

        private String[] fieldNames;

        @Override
        public void initialize(AtLeastOneNotNull constraintAnnotation) {
            this.fieldNames = constraintAnnotation.fieldNames();
        }

        @Override
        public boolean isValid(Object value, ConstraintValidatorContext context) {
            if (value == null) {
                return true;
            }

            try {
                for (String fieldName : fieldNames) {
                    Object property = BeanUtils.getPropertyDescriptor(value.getClass(), fieldName).getReadMethod().invoke(value);
                    if (property != null) {
                        return true;
                    }
                }
                return false;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }
}
