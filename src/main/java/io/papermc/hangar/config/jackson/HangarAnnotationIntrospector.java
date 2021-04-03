package io.papermc.hangar.config.jackson;

import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import io.papermc.hangar.model.common.NamedPermission;
import io.papermc.hangar.model.common.Permission;
import io.papermc.hangar.security.HangarAuthenticationToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;

public class HangarAnnotationIntrospector extends JacksonAnnotationIntrospector {

    private static final Logger logger = LoggerFactory.getLogger(HangarAnnotationIntrospector.class);

    @Override
    protected boolean _isIgnorable(Annotated a) {
        if (a.hasAnnotation(RequiresPermission.class)) {
            logger.debug("Found " + RequiresPermission.class + " annotation on " + a.getName());
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (!authentication.isAuthenticated() || !(authentication instanceof HangarAuthenticationToken)) {
                return true;
            }
            Permission perms = ((HangarAuthenticationToken) authentication).getPrincipal().getGlobalPermissions();
            NamedPermission[] requiredPerms = a.getAnnotation(RequiresPermission.class).value();
            if (!perms.hasAll(requiredPerms)) {
                return true;
            }
            logger.debug(authentication.getName() + " has required permissions: " + Arrays.toString(requiredPerms));
        }
        return super._isIgnorable(a);
    }

}
