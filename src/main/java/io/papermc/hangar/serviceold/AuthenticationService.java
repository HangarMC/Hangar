package io.papermc.hangar.serviceold;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.papermc.hangar.config.hangar.HangarConfig;
import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.daoold.ApiKeyDao;
import io.papermc.hangar.db.daoold.api.SessionsDao;
import io.papermc.hangar.db.modelold.UsersTable;
import io.papermc.hangar.model.internal.ChangeAvatarToken;
import io.papermc.hangar.modelold.ApiAuthInfo;
import io.papermc.hangar.util.AuthUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.annotation.RequestScope;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service("oldAuthenticationService")
public class AuthenticationService extends HangarService {


    private final HangarConfig hangarConfig;
    private final HangarDao<SessionsDao> sessionsDao;
    private final HangarDao<ApiKeyDao> apiKeyDao;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private final HttpServletRequest request;

    private static final Pattern API_ROUTE_PATTERN = Pattern.compile("^/api/(?!old).+");
    private static final String UUID_REGEX = "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}";
    private static final Pattern API_KEY_PATTERN = Pattern.compile("(" + UUID_REGEX + ").(" + UUID_REGEX + ")");

    @Autowired
    public AuthenticationService(HangarConfig hangarConfig, HangarDao<SessionsDao> sessionsDao, HangarDao<ApiKeyDao> apiKeyDao, RestTemplate restTemplate, ObjectMapper objectMapper, HttpServletRequest request, Supplier<Optional<UsersTable>> currentUser) {
        this.hangarConfig = hangarConfig;
        this.sessionsDao = sessionsDao;
        this.apiKeyDao = apiKeyDao;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.request = request;
        this.currentUser = currentUser;
    }


    @Bean
    @RequestScope
    @Deprecated
    public ApiAuthInfo apiAuthInfo() {
        Matcher apiMatcher = API_ROUTE_PATTERN.matcher(request.getRequestURI());
        if (apiMatcher.matches()) { // if api method
            AuthUtils.AuthCredentials credentials = AuthUtils.parseAuthHeader(request, true);
            if (credentials.getSession() == null) {
                throw AuthUtils.unAuth("No session specified");
            }
            ApiAuthInfo info = apiKeyDao.get().getApiAuthInfo(credentials.getSession());
            if (info == null) {
                throw AuthUtils.unAuth("Invalid session");
            }
            if (info.getExpires().isBefore(OffsetDateTime.now())) {
                sessionsDao.get().delete(credentials.getSession());
                throw AuthUtils.unAuth("Api session expired");
            }
            return info;
        }
        return null;
    }

    public URI changeAvatarUri(String requester, String organization) throws JsonProcessingException {
        ChangeAvatarToken token = getChangeAvatarToken(requester, organization);
        UriComponentsBuilder uriComponents = UriComponentsBuilder.fromHttpUrl(hangarConfig.getAuthUrl());
        uriComponents.path("/accounts/user/{organization}/change-avatar/").queryParam("key", token.getSignedData());
        return uriComponents.build(organization);
    }

    public ChangeAvatarToken getChangeAvatarToken(String requester, String organization) throws JsonProcessingException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> bodyMap = new LinkedMultiValueMap<>();
        bodyMap.add("api-key", hangarConfig.sso.getApiKey());
        bodyMap.add("request_username", requester);
        ChangeAvatarToken token;
        token = objectMapper.treeToValue(restTemplate.postForObject(hangarConfig.security.api.getUrl() + "/api/users/" + organization + "/change-avatar-token/", new HttpEntity<>(bodyMap, headers), ObjectNode.class), ChangeAvatarToken.class);
        return token;
    }

}
