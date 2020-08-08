package me.minidigger.hangar.controller.resolvers;

import me.minidigger.hangar.db.dao.ApiKeyDao;
import me.minidigger.hangar.db.dao.HangarDao;
import me.minidigger.hangar.model.ApiAuthInfo;
import me.minidigger.hangar.service.AuthenticationService;
import me.minidigger.hangar.service.AuthenticationService.AuthCredentials;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;

public class ApiAuthInfoMethodArgumentResolver implements HandlerMethodArgumentResolver {

    private final HangarDao<ApiKeyDao> apiKeyDao;
    private final AuthenticationService authenticationService;

    public ApiAuthInfoMethodArgumentResolver(HangarDao<ApiKeyDao> apiKeyDao, AuthenticationService authenticationService) {
        this.apiKeyDao = apiKeyDao;
        this.authenticationService = authenticationService;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType() == ApiAuthInfo.class;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        if (request == null) return null;
        AuthCredentials authCredentials = authenticationService.parseAuthHeader(request);
        return apiKeyDao.get().getApiAuthInfo(authCredentials.getSession());
    }
}
