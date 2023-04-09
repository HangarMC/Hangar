package io.papermc.hangar.security.annotations.currentuser;

import java.util.Collection;
import java.util.Set;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.annotation.AnnotationMetadataExtractor;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserMetadataExtractor implements AnnotationMetadataExtractor<CurrentUser> {

    private final ExpressionParser expressionParser = new SpelExpressionParser();

    @Override
    public Collection<? extends ConfigAttribute> extractAttributes(final CurrentUser securityAnnotation) {
        return Set.of(new CurrentUserAttribute(this.expressionParser.parseExpression(securityAnnotation.userArgument())));
    }

    record CurrentUserAttribute(Expression expression) implements ConfigAttribute {

        @Override
        public String getAttribute() {
            return null;
        }
    }
}
