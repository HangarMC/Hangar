package io.papermc.hangar.controller.extras.resolvers.path.model;

import io.papermc.hangar.exceptions.HangarApiException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.servlet.mvc.method.annotation.PathVariableMethodArgumentResolver;

public abstract class HangarModelPathVarResolver<M> extends PathVariableMethodArgumentResolver {

    protected abstract Class<M> modelType();

    @Override
    public boolean supportsParameter(final @NotNull MethodParameter parameter) {
        return super.supportsParameter(parameter) && parameter.getParameterType().equals(this.modelType());
    }

    @Override
    protected void handleMissingValue(final @NotNull String name, final @NotNull MethodParameter parameter) {
        throw new HangarApiException(HttpStatus.NOT_FOUND);
    }

    @Override
    protected final @Nullable M resolveName(final @NotNull String name, final @NotNull MethodParameter parameter, final @NotNull NativeWebRequest request) throws Exception {
        final String param = (String) super.resolveName(name, parameter, request);
        if (param == null) {
            return null;
        }
        return this.resolveParameter(param);
    }

    protected abstract M resolveParameter(@NotNull String param);
}
