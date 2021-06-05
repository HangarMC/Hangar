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
    public boolean supportsParameter(@NotNull MethodParameter parameter) {
        return super.supportsParameter(parameter) && parameter.getParameterType().equals(modelType());
    }

    @Override
    protected void handleMissingValue(@NotNull String name, @NotNull MethodParameter parameter) {
        throw new HangarApiException(HttpStatus.NOT_FOUND);
    }

    @Nullable
    @Override
    protected final M resolveName(@NotNull String name, @NotNull MethodParameter parameter, @NotNull NativeWebRequest request) throws Exception {
        String param = (String) super.resolveName(name, parameter, request);
        if (param == null) {
            return null;
        }
        return resolveParameter(param);
    }

    protected abstract M resolveParameter(@NotNull String param);
}
