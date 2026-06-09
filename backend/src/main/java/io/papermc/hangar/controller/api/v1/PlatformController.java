package io.papermc.hangar.controller.api.v1;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.controller.api.v1.interfaces.IPlatformController;
import io.papermc.hangar.model.api.platform.PlatformVersion;
import io.papermc.hangar.model.common.Platform;
import io.papermc.hangar.security.annotations.ratelimit.RateLimit;
import io.papermc.hangar.service.internal.PlatformService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RateLimit(path = "platforms")
public class PlatformController extends HangarComponent implements IPlatformController {

    private final PlatformService platformService;

    @Autowired
    public PlatformController(final PlatformService platformService) {
        this.platformService = platformService;
    }

    @Override
    @ResponseBody
    public List<PlatformVersion> getPlatformVersions(final Platform platform) {
        return this.platformService.getDescendingVersionsForPlatform(platform);
    }
}
