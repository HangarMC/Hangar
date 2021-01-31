package io.papermc.hangar.service;

import io.papermc.hangar.config.hangar.HangarConfig;
import io.papermc.hangar.controller.extras.HangarApiRequest;
import io.papermc.hangar.controller.extras.HangarRequest;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class HangarService {

    @Autowired
    protected HangarConfig hangarConfig;

    @Autowired
    protected HangarApiRequest hangarApiRequest;

    @Autowired
    protected HangarRequest hangarRequest;
}
