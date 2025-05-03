package io.papermc.hangar.controller.extras.resolvers.path.model;

import io.papermc.hangar.exceptions.HangarApiException;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ValueConstants;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.method.annotation.AbstractNamedValueMethodArgumentResolver;
import org.springframework.web.servlet.HandlerMapping;

/**
 * Model resolver that is able to resolve both path variables and request params
 *
 * @param <M> the model type
 */
public abstract class HangarModelResolver<M> extends AbstractNamedValueMethodArgumentResolver {

    protected abstract Class<M> modelType();

    @Override
    public boolean supportsParameter(final @NotNull MethodParameter parameter) {
        if (!parameter.hasParameterAnnotation(PathVariable.class) && !parameter.hasParameterAnnotation(RequestParam.class)) {
            return false;
        }
        return parameter.getParameterType().equals(this.modelType());
    }

    @Override
    protected @NotNull NamedValueInfo createNamedValueInfo(MethodParameter parameter) {
        final PathVariable pathVariable = parameter.getParameterAnnotation(PathVariable.class);
        if (pathVariable != null) {
            return new NamedValueInfo(pathVariable.name(), pathVariable.required(), ValueConstants.DEFAULT_NONE);
        }
        final RequestParam requestParam = parameter.getParameterAnnotation(RequestParam.class);
        if (requestParam != null) {
            return new NamedValueInfo(requestParam.name(), requestParam.required(), ValueConstants.DEFAULT_NONE);
        }
        throw new IllegalStateException("No PathVariable or RequestParam annotation found on parameter " + parameter.getParameterName() + " of method " + parameter.getMethod());
    }

    @Override
    protected final @Nullable M resolveName(final @NotNull String name, final @NotNull MethodParameter parameter, final @NotNull NativeWebRequest request) throws Exception {
        final PathVariable pathVariable = parameter.getParameterAnnotation(PathVariable.class);
        if (pathVariable != null) {
            @SuppressWarnings("unchecked") final Map<String, String> uriTemplateVars = (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST);
            final String param = uriTemplateVars != null ? uriTemplateVars.get(name) : null;
            if (param == null) {
                if (pathVariable.required()) {
                    throw new HangarApiException(HttpStatus.BAD_REQUEST, "Missing required path variable '" + name + "'");
                }
                return null;
            }
            return this.handleValue(this.resolveParameter(param, request), param, name, "path variable");
        }
        final RequestParam requestParam = parameter.getParameterAnnotation(RequestParam.class);
        if (requestParam != null) {
            final String[] paramArray = request.getParameterValues(name);
            if (paramArray == null) {
                if (requestParam.required()) {
                    throw new HangarApiException(HttpStatus.BAD_REQUEST, "Missing required request param '" + name + "'");
                }
                return null;
            }
            Assert.state(paramArray.length == 1, "Expected single value for parameter '" + name + "' but got " + paramArray.length);
            return this.handleValue(this.resolveParameter(paramArray[0], request), paramArray[0], name, "request param");
        }
        return null;
    }

    private M handleValue(final M resolvedValue, final String value, final String name, final String type) {
        if (resolvedValue == null) {
            throw new HangarApiException(HttpStatus.NOT_FOUND, "Unknown value '" + value + "' for " + type + " '" + name + "'");
        }
        return resolvedValue;
    }

    protected boolean supportsId(final NativeWebRequest request) {
        return !"false".equals(request.getParameter("resolveId"));
    }

    protected abstract M resolveParameter(@NotNull String param, NativeWebRequest request);
}
