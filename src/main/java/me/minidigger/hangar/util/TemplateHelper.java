package me.minidigger.hangar.util;

import me.minidigger.hangar.config.hangar.HangarConfig;
import me.minidigger.hangar.db.model.ProjectsTable;
import me.minidigger.hangar.model.viewhelpers.ProjectData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

@Component
public class TemplateHelper {

    private final RouteHelper routeHelper;
    private final HangarConfig hangarConfig;

    @Autowired
    public TemplateHelper(RouteHelper routeHelper, HangarConfig hangarConfig) {
        this.routeHelper = routeHelper;
        this.hangarConfig = hangarConfig;
    }

    public String format(String s, String...args) {
        return String.format(s, args);
    }

    public String urlEncode(String string) {
        return URLEncoder.encode(string, StandardCharsets.UTF_8);
    }

    public String avatarUrl(String name) {
        return String.format(hangarConfig.security.api.getAvatarUrl(), name);
    }

    public String projectAvatarUrl(ProjectsTable table) {
        return routeHelper.getRouteUrl("projects.showIcon", table.getOwnerName(), table.getSlug());
    }

    public String formatFileSize(Long size) {
        if (size < 1024) {
            return size + "B";
        }
        else {
            long z = (63L - Long.numberOfLeadingZeros(size)) / 10L;
            return String.format("%.1f %cB", size.doubleValue() / (1L << (z * 10)), " KMGTPE".charAt((int) z));
        }
    }

    public String prettifyDateTime(OffsetDateTime dateTime) {
        return DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).format(dateTime);
    }

    public String prettifyDate(OffsetDateTime dateTime) {
        return DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).format(dateTime.toLocalDate());
    }
}
