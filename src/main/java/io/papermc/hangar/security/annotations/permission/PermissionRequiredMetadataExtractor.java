package io.papermc.hangar.security.annotations.permission;

import io.papermc.hangar.model.common.NamedPermission;
import io.papermc.hangar.model.common.PermissionType;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.annotation.AnnotationMetadataExtractor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

@Component
public class PermissionRequiredMetadataExtractor implements AnnotationMetadataExtractor<PermissionRequired> {

    private final ExpressionParser expressionParser = new SpelExpressionParser();

    @Override
    public Collection<? extends ConfigAttribute> extractAttributes(PermissionRequired securityAnnotation) {
        return Set.of(new PermissionRequiredAttribute(
                securityAnnotation.type(),
                securityAnnotation.perms(),
                expressionParser.parseExpression(securityAnnotation.args())
        ));
    }

    static class PermissionRequiredAttribute implements ConfigAttribute {

        private final PermissionType permissionType;
        private final NamedPermission[] permissions;
        private final Expression expression;

        PermissionRequiredAttribute(PermissionType permissionType, NamedPermission[] permissions, Expression expression) {
            this.permissionType = permissionType;
            this.permissions = permissions;
            this.expression = expression;
        }

        public PermissionType getPermissionType() {
            return permissionType;
        }

        public NamedPermission[] getPermissions() {
            return permissions;
        }

        public Expression getExpression() {
            return expression;
        }

        @Override
        public String getAttribute() {
            return null;
        }

        @Override
        public String toString() {
            return "PermissionRequiredAttribute{" +
                    "permissionType=" + permissionType +
                    ", permissions=" + Arrays.toString(permissions) +
                    '}';
        }
    }
}
