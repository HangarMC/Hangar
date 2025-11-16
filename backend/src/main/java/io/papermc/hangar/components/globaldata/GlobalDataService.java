package io.papermc.hangar.components.globaldata;

import io.papermc.hangar.components.globaldata.GlobalDataService.GlobalData.Announcement;
import io.papermc.hangar.components.globaldata.dao.AnnouncementTable;
import io.papermc.hangar.components.globaldata.dao.GlobalDataDAO;
import io.papermc.hangar.components.globaldata.dao.GlobalNotificationTable;
import io.papermc.hangar.config.CacheConfig;
import io.papermc.hangar.model.common.Platform;
import io.papermc.hangar.model.api.platform.PlatformVersion;
import io.papermc.hangar.service.internal.PlatformService;
import jakarta.validation.Valid;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import static io.papermc.hangar.components.globaldata.GlobalDataService.GlobalData.PlatformData;

@Service
public class GlobalDataService {

    private final GlobalDataDAO globalDataDAO;
    private final PlatformService platformService;

    public GlobalDataService(final GlobalDataDAO globalDataDAO, final PlatformService platformService) {
        this.globalDataDAO = globalDataDAO;
        this.platformService = platformService;
    }

    @Cacheable(CacheConfig.GLOBAL_DATA)
    public GlobalData getGlobalData() {
        var globalNotifications = this.globalDataDAO.getActiveGlobalNotifications();
        var announcements = this.getAnnouncements().stream().map(a -> new Announcement(a.getText(), a.getColor())).toList();
        var platformData = Arrays.stream(Platform.values())
            .map(platform -> new PlatformData(platform.getName(), platform.getCategory(), platform.getUrl(), platform.getEnumName(), platform.isVisible(), this.platformService.getDescendingVersionsForPlatform(platform)))
            .toList();
        return new GlobalData(globalNotifications, announcements, platformData);
    }

    public record GlobalData(
        Map<String, String> globalNotifications,
        List<Announcement> announcements,
        List<PlatformData> platforms
    ) {

        public record PlatformData(String name,
                                   Platform.Category category,
                                   String url,
                                   String enumName,
                                   boolean visible,
                                   List<PlatformVersion> platformVersions) {
        }

        public record Announcement(String text, String color) {
        }
    }

    public List<AnnouncementTable> getAnnouncements() {
        return this.globalDataDAO.getAnnouncements();
    }

    @CacheEvict(value = CacheConfig.GLOBAL_DATA, allEntries = true)
    public void updateAnnouncements(@Valid List<AnnouncementTable> announcements) {
        this.globalDataDAO.updateAnnouncements(announcements);
    }

    public List<GlobalNotificationTable> getGlobalNotifications() {
        return this.globalDataDAO.getGlobalNotifications();
    }

    @CacheEvict(value = CacheConfig.GLOBAL_DATA, allEntries = true)
    public void updateGlobalNotifications(@Valid List<GlobalNotificationTable> globalNotifications) {
        this.globalDataDAO.updateGlobalNotifications(globalNotifications);
    }

    @CacheEvict(value = CacheConfig.GLOBAL_DATA, allEntries = true)
    public void updatePlatformVersions(@Valid GlobalDataController.UpdatePlatformVersionsForm form) {
        this.platformService.updatePlatformVersions(form);
    }
}
