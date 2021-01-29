package io.papermc.hangar.service;

import io.papermc.hangar.controller.extras.HangarApiRequest;
import io.papermc.hangar.controller.extras.HangarRequest;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class HangarService {

    @Autowired
    protected HangarApiRequest hangarApiRequest;

    @Autowired
    protected HangarRequest hangarRequest;
}
