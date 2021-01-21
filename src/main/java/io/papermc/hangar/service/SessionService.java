package io.papermc.hangar.service;

import io.papermc.hangar.config.hangar.HangarConfig;
import io.papermc.hangar.db.UserSessionDao;
import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.modelold.UserSessionsTable;
import io.papermc.hangar.db.modelold.UsersTable;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class SessionService {

    private final HangarConfig hangarConfig;
    private final HangarDao<UserSessionDao> userSessionDao;

    public SessionService(HangarConfig hangarConfig, HangarDao<UserSessionDao> userSessionDao) {
        this.hangarConfig = hangarConfig;
        this.userSessionDao = userSessionDao;
    }

    public UserSessionsTable createSession(UsersTable usersTable) {
        return userSessionDao.get().insert(new UserSessionsTable(
                OffsetDateTime.now().plus(hangarConfig.session.getMaxAge().toMillis(), ChronoUnit.MILLIS),
                UUID.randomUUID().toString(),
                usersTable.getId()
        ));
    }

    public UserSessionsTable getSession(String token) {
        return userSessionDao.get().getByToken(token);
    }

    public void deleteSession(UserSessionsTable sessionsTable) {
        userSessionDao.get().delete(sessionsTable);
    }
}
