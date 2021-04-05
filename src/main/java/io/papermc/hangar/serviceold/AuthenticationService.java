package io.papermc.hangar.serviceold;

import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.daoold.ApiKeyDao;
import io.papermc.hangar.db.daoold.api.SessionsDao;
import io.papermc.hangar.db.modelold.UsersTable;
import io.papermc.hangar.modelold.ApiAuthInfo;
import io.papermc.hangar.util.AuthUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.RequestScope;

import javax.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service("oldAuthenticationService")
public class AuthenticationService extends HangarService {


    private final HangarDao<SessionsDao> sessionsDao;
    private final HangarDao<ApiKeyDao> apiKeyDao;

    private final HttpServletRequest request;

    private static final Pattern API_ROUTE_PATTERN = Pattern.compile("^/api/(?!old).+");
    private static final String UUID_REGEX = "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}";
    private static final Pattern API_KEY_PATTERN = Pattern.compile("(" + UUID_REGEX + ").(" + UUID_REGEX + ")");

    @Autowired
    public AuthenticationService(HangarDao<SessionsDao> sessionsDao, HangarDao<ApiKeyDao> apiKeyDao, HttpServletRequest request, Supplier<Optional<UsersTable>> currentUser) {
        this.sessionsDao = sessionsDao;
        this.apiKeyDao = apiKeyDao;
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

}
