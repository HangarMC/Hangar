package io.papermc.hangar.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.papermc.hangar.config.hangar.HangarConfig;
import io.papermc.hangar.db.model.ProjectsTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Map;

@Component
public class TemplateHelper {

    private final ObjectMapper mapper;
    private final HangarConfig hangarConfig;

    @Autowired
    public TemplateHelper(ObjectMapper mapper, HangarConfig hangarConfig) {
        this.mapper = mapper;
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
        return Routes.getRouteUrlOf("projects.showIcon", table.getOwnerName(), table.getSlug());
    }

    public String formatFileSize(Long size) {
        if (size == null) return "";
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

    public ArrayNode serializeMap(Map<?, ?> map) {
        ArrayNode arrayNode = mapper.createArrayNode();
        map.forEach((o, o2) -> {
            ObjectNode objectNode = mapper.createObjectNode();
            objectNode.set("key", mapper.valueToTree(o));
            objectNode.set("value", mapper.valueToTree(o2));
            arrayNode.add(objectNode);
        });
        return arrayNode;
    }
}
