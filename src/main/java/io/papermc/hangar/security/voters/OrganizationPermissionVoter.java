package io.papermc.hangar.security.voters;

import io.papermc.hangar.security.attributes.PermissionAttribute;
import io.papermc.hangar.service.PermissionService;
import org.springframework.web.bind.annotation.PathVariable;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Set;

public class OrganizationPermissionVoter extends HangarPermissionVoter {
    public OrganizationPermissionVoter(PermissionService permissionService) {
        super(
                (hangarAuthentication, methodInvocation) -> {
                    Method method = methodInvocation.getMethod();
                    if (method.getParameterCount() == 0) return Set.of();
                    String organizationName = null;
                    Object[] arguments = methodInvocation.getArguments();
                    for (int i = 0; i < method.getParameters().length; i++) {
                        Parameter parameter = method.getParameters()[i];
                        if (parameter.isAnnotationPresent(PathVariable.class)) {
                            PathVariable pathVariable = parameter.getAnnotation(PathVariable.class);
                            if (pathVariable.name().equalsIgnoreCase("organization")) {
                                organizationName = arguments[i].toString();
                                continue;
                            } else if (pathVariable.value().equalsIgnoreCase("organization")) {
                                organizationName = arguments[i].toString();
                                continue;
                            } else if (parameter.getName().equalsIgnoreCase("organization")) {
                                organizationName = arguments[i].toString();
                                continue;
                            }
                        }
                    }
                    if (organizationName == null) {
                        return Set.of();
                    }
                    return permissionService.getOrganizationPermissions(hangarAuthentication.getUserId(), organizationName).toNamed();
                },
                hangarAuthentication -> true,
                permissionAttribute -> permissionAttribute.getType().equalsIgnoreCase(PermissionAttribute.ORG_TYPE)
        );
    }
}
