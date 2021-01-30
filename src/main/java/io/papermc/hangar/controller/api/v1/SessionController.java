package io.papermc.hangar.controller.api.v1;

import io.papermc.hangar.controller.api.v1.interfaces.ISessionsController;
import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.internal.table.auth.ApiSessionDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class SessionController extends HangarApiController implements ISessionsController {

    private final ApiSessionDAO apiSessionDAO;

    @Autowired
    public SessionController(HangarDao<ApiSessionDAO> apiSessionDAO) {
        this.apiSessionDAO = apiSessionDAO.get();
    }

    @Override
    public ResponseEntity<Void> deleteSession() {
        apiSessionDAO.delete(hangarApiRequest.getSession());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
