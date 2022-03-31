package io.papermc.hangar.security.voters;

import io.papermc.hangar.security.attributes.PermissionAttribute;
import io.papermc.hangar.service.PermissionService;
import org.springframework.web.bind.annotation.PathVariable;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Set;

public class ProjectPermissionVoter extends HangarPermissionVoter {
    public ProjectPermissionVoter(PermissionService permissionService) {
        super(
                (hangarAuthentication, methodInvocation) -> {
                    Method method = methodInvocation.getMethod();
                    if (method.getParameterCount() == 0) return Set.of();
                    String author = null;
                    String slug = null;
                    Object[] arguments = methodInvocation.getArguments();
                    for (int i = 0; i < method.getParameters().length; i++) {
                        Parameter parameter = method.getParameters()[i];
                        if (parameter.isAnnotationPresent(PathVariable.class)) { // TODO too tired to figure out what the nicer looking way of doing this is
                            PathVariable pathVarAnnotation = parameter.getAnnotation(PathVariable.class);
                            if (pathVarAnnotation.name().equalsIgnoreCase("author")) {
                                author = arguments[i].toString();
                                continue;
                            }
                            else if (pathVarAnnotation.value().equalsIgnoreCase("author")) {
                                author = arguments[i].toString();
                                continue;
                            } else if (parameter.getName().equalsIgnoreCase("author")) {
                                author = arguments[i].toString();
                                continue;
                            }

                            if (pathVarAnnotation.name().equalsIgnoreCase("slug")) {
                                slug = arguments[i].toString();
                                continue;
                            } else if (pathVarAnnotation.value().equalsIgnoreCase("slug")) {
                                slug = arguments[i].toString();
                                continue;
                            } else if (parameter.getName().equalsIgnoreCase("slug")) {
                                slug = arguments[i].toString();
                                continue;
                            }
                        }
                    }
                    if (author == null || slug == null) {
                        return Set.of();
                    }
                    return permissionService.getProjectPermissions(hangarAuthentication.getUserId(), author, slug).toNamed();
                },
                hangarAuthentication -> true,
                permissionAttribute -> permissionAttribute.getType().equals(PermissionAttribute.PROJECT_TYPE)
        );
    }
}
