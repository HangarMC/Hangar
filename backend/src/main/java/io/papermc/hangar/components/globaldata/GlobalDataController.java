package io.papermc.hangar.components.globaldata;

import io.papermc.hangar.components.globaldata.GlobalDataService.GlobalData;
import io.papermc.hangar.components.globaldata.dao.AnnouncementTable;
import io.papermc.hangar.components.globaldata.dao.GlobalNotificationTable;
import io.papermc.hangar.model.common.NamedPermission;
import io.papermc.hangar.model.common.Platform;
import io.papermc.hangar.security.annotations.Anyone;
import io.papermc.hangar.security.annotations.permission.PermissionRequired;
import io.papermc.hangar.security.annotations.ratelimit.RateLimit;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.EnumMap;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;


@RestController
@Anyone
@RateLimit(path = "globaldata")
@RequestMapping(path = "/api/internal/globalData", produces = MediaType.APPLICATION_JSON_VALUE)
public class GlobalDataController {

    private final GlobalDataService globalDataService;

    public GlobalDataController(final GlobalDataService globalDataService) {
        this.globalDataService = globalDataService;
    }

    @GetMapping("/")
    public GlobalData getGlobalData() {
       return this.globalDataService.getGlobalData();
    }

    @GetMapping("/announcements")
    public List<AnnouncementTable> getAnnouncements() {
        return this.globalDataService.getAnnouncements();
    }

    @PostMapping("/announcements")
    public void updateAnnouncements(@RequestBody @Valid final List<AnnouncementTable> announcements) {
        this.globalDataService.updateAnnouncements(announcements);
    }

    @GetMapping("/notifications")
    public List<GlobalNotificationTable> getGlobalNotifications() {
        return this.globalDataService.getGlobalNotifications();
    }

    @PostMapping("/notifications")
    public void updateGlobalNotifications(@RequestBody @Valid final List<GlobalNotificationTable> globalNotification) {
        this.globalDataService.updateGlobalNotifications(globalNotification);
    }

    public static class UpdatePlatformVersionsForm extends EnumMap<Platform, @Size(min = 1) List<@NotBlank String>> {
        public UpdatePlatformVersionsForm() {
            super(Platform.class);
        }
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping(path = "/platformVersions", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PermissionRequired(NamedPermission.MANUAL_VALUE_CHANGES)
    public void updatePlatformVersions(@RequestBody @Valid final UpdatePlatformVersionsForm form) {
        this.globalDataService.updatePlatformVersions(form);
    }
}
