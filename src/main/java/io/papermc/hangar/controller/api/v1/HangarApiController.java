package io.papermc.hangar.controller.api.v1;

import io.papermc.hangar.controller.RequestController;
import io.papermc.hangar.controller.extras.HangarApiRequest;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class HangarApiController extends RequestController {

    @Autowired
    protected HangarApiRequest hangarApiRequest;
}
