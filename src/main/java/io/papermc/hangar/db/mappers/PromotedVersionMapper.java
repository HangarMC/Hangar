package io.papermc.hangar.db.mappers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import io.papermc.hangar.db.customtypes.JSONB;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.api.project.version.PromotedVersion;
import io.papermc.hangar.model.api.project.version.PromotedVersionTag;
import io.papermc.hangar.model.common.TagColor;
import io.papermc.hangar.util.StringUtils;
import org.jdbi.v3.core.mapper.ColumnMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.springframework.http.HttpStatus;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PromotedVersionMapper implements ColumnMapper<List<PromotedVersion>> {

    @Override
    public List<PromotedVersion> map(ResultSet rs, int column, StatementContext ctx) throws SQLException {
        ObjectMapper mapper = new ObjectMapper();
        List<PromotedVersion> promotedVersions = new ArrayList<>();
        JsonNode jsonNode = ((JSONB) rs.getObject(column)).getJson();
        if (!jsonNode.isArray()) {
            throw new IllegalArgumentException("Must be JSON array");
        }
        ArrayNode jsons = (ArrayNode) jsonNode;
        jsons.forEach(json -> {
            String version;
            String tagName;
            if(json.get("version_string") == null){
                version = "1.0.0";
            } else {
                version = json.get("version_string").asText();
            }
            if(json.get("tag_name") == null){
                tagName = "tag_name";
            } else {
                tagName = json.get("tag_name").asText();
            }
            List<String> minecraftVersions = null;
            String data = null;
            if (json.has("tag_version") && json.get("tag_version").isArray()) {
                try {
                    minecraftVersions = (List<String>) mapper.treeToValue(json.get("tag_version"), List.class);
                    data = StringUtils.formatVersionNumbers(minecraftVersions);
                } catch (JsonProcessingException exception) {
                    exception.printStackTrace();
                    throw new HangarApiException(HttpStatus.BAD_REQUEST, "Error mapping promoted versions");
                }
            }
            TagColor color;
            if(json.get("tag_color") == null){
                color = TagColor.UNSTABLE;
            } else {
                color = TagColor.getValues()[json.get("tag_color").asInt()];
            }

            promotedVersions.add(
                    new PromotedVersion(
                            version,
                            List.of(
                                    new PromotedVersionTag(
                                            tagName,
                                            data,
                                            data,
                                            minecraftVersions,
                                            color
                                    )
                            )
                    )
            );
        });
        return promotedVersions;
    }
}
