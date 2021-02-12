package io.papermc.hangar.config.jackson;

import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import io.papermc.hangar.model.common.NamedPermission;
import io.papermc.hangar.model.common.Permission;
import io.papermc.hangar.security.HangarAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class HangarAnnotationIntrospector extends JacksonAnnotationIntrospector {

    @Override
    protected boolean _isIgnorable(Annotated a) {
        if (a.hasAnnotation(RequiresPermission.class)) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (!authentication.isAuthenticated() || !(authentication instanceof HangarAuthenticationToken)) {
                return true;
            }
            Permission perms = ((HangarAuthenticationToken) authentication).getPrincipal().getGlobalPermissions();
            NamedPermission[] requiredPerms = a.getAnnotation(RequiresPermission.class).value();
            if (!perms.hasAll(requiredPerms)) {
                return true;
            }
        }
        return super._isIgnorable(a);
    }

}
