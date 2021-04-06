package io.papermc.hangar.controller.api.v1;

import io.papermc.hangar.controller.api.v1.interfaces.IApiKeysController;
import io.papermc.hangar.model.common.NamedPermission;
import io.papermc.hangar.model.internal.api.requests.CreateAPIKeyForm;
import io.papermc.hangar.security.annotations.permission.PermissionRequired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
@PermissionRequired(NamedPermission.EDIT_API_KEYS)
public class ApiKeysController implements IApiKeysController {

    @Override
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public String createKey(CreateAPIKeyForm apiKeyForm) {
        // TODO implement
        System.out.println(apiKeyForm);
        return new String("HELLO THERE");
    }

    @Override
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteKey(String name) {
        // TODO implement
        System.out.println(name);
    }
}
