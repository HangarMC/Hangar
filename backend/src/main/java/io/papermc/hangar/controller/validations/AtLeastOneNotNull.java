package io.papermc.hangar.controller.validations;

import org.springframework.beans.BeanUtils;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AtLeastOneNotNull.Validator.class)
@Documented
public @interface AtLeastOneNotNull {
    String[] fieldNames();
    String message() default "Must have one non null field";
    boolean includeBlankStrings() default false;
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    class Validator implements ConstraintValidator<AtLeastOneNotNull, Object> {

        private String[] fieldNames;

        @Override
        public void initialize(final AtLeastOneNotNull constraintAnnotation) {
            this.fieldNames = constraintAnnotation.fieldNames();
        }

        @Override
        public boolean isValid(final Object value, final ConstraintValidatorContext context) {
            if (value == null) {
                return true;
            }

            try {
                for (final String fieldName : this.fieldNames) {
                    final PropertyDescriptor propertyDescriptor = BeanUtils.getPropertyDescriptor(value.getClass(), fieldName);
                    if (propertyDescriptor != null) {
                        final Object property = propertyDescriptor.getReadMethod().invoke(value);
                        if (property != null) {
                            if (property instanceof String) {
                                return !((String) property).isBlank();
                            } else {
                                return true;
                            }
                        }
                    }
                }
                return false;
            } catch (final Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }
}
