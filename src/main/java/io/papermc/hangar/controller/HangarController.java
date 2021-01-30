package io.papermc.hangar.controller;

import io.papermc.hangar.controller.extras.HangarRequest;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class HangarController extends RequestController {

    @Autowired
    protected HangarRequest hangarRequest;
}
