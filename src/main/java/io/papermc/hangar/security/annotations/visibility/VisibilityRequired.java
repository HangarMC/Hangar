package io.papermc.hangar.security.annotations.visibility;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Set;

/**
 * Visibility check for a project or version
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface VisibilityRequired {
    /**
     * Project or Version
     * @return type to check
     */
    Type type();

    /**
     * Method arguments to resolve the project or version
     * @return method arguments as an SpEL array
     */
    String args();

    enum Type {
        PROJECT(1, 2),
        VERSION(1, 4);

        private final Set<Integer> argCount;

        Type(Integer...argCounts) {
            this.argCount = Set.of(argCounts);
        }

        public Set<Integer> getArgCount() {
            return argCount;
        }
    }
}
