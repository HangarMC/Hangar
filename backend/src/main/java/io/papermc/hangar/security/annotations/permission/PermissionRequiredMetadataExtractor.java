package io.papermc.hangar.security.annotations.permission;

import io.papermc.hangar.model.common.NamedPermission;
import io.papermc.hangar.model.common.PermissionType;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.annotation.AnnotationMetadataExtractor;
import org.springframework.stereotype.Component;

@Component
public class PermissionRequiredMetadataExtractor implements AnnotationMetadataExtractor<PermissionRequired> {

    private final ExpressionParser expressionParser = new SpelExpressionParser();

    @Override
    public Collection<? extends ConfigAttribute> extractAttributes(final PermissionRequired securityAnnotation) {
        return Set.of(new PermissionRequiredAttribute(
            securityAnnotation.type(),
            securityAnnotation.perms(),
            this.expressionParser.parseExpression(securityAnnotation.args())
        ));
    }

    record PermissionRequiredAttribute(PermissionType permissionType, NamedPermission[] permissions, Expression expression) implements ConfigAttribute {

        @Override
        public String getAttribute() {
            return null;
        }
    }
}
