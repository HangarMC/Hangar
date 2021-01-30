package io.papermc.hangar.db.mappers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;
import io.papermc.hangar.db.customtypes.JSONB;
import io.papermc.hangar.model.TagColor;
import io.papermc.hangar.model.api.project.version.PromotedVersion;
import io.papermc.hangar.model.api.project.version.PromotedVersionTag;
import io.papermc.hangar.util.StringUtils;
import org.jdbi.v3.core.mapper.ColumnMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.jetbrains.annotations.Nullable;

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
            String version = json.get("version_string").asText();
            String tagName = json.get("tag_name").asText();
            String data = null;
            if (json.has("tag_version") && json.get("tag_version").isArray()) {
                try {
                    data = StringUtils.formatVersionNumbers((List<String>) mapper.treeToValue(json.get("tag_version"), List.class));
                } catch (JsonProcessingException exception) {
                    exception.printStackTrace();
                }
            }
            TagColor color = TagColor.VALUES[json.get("tag_color").asInt()];
            // TODO a whole bunch
//            val displayAndMc = data.map { rawData =>
//                lazy val lowerBoundVersion = for {
//                    range <- Try(VersionRange.createFromVersionSpec(rawData)).toOption
//                    version <- Option(range.getRecommendedVersion)
//                            .orElse(range.getRestrictions.asScala.flatMap(r => Option(r.getLowerBound)).toVector.minimumOption)
//                } yield version
//
//                lazy val lowerBoundVersionStr = lowerBoundVersion.map(_.toString)
//
//                def unzipOptions[A, B](fab: Option[(A, B)]): (Option[A], Option[B]) = fab match {
//                    case Some((a, b)) => Some(a) -> Some(b)
//                    case None         => (None, None)
//                }
//
//                tagName match {
//                    case "Sponge" =>
//                        lowerBoundVersionStr.collect {
//                        case MajorMinor(version) => version
//                    } -> None //TODO
//                    case "SpongeForge" =>
//                        unzipOptions(
//                                lowerBoundVersionStr.collect {
//                        case SpongeForgeMajorMinorMC(version, mcVersion) => version -> mcVersion
//                    }
//                )
//                    case "SpongeVanilla" =>
//                        unzipOptions(
//                                lowerBoundVersionStr.collect {
//                        case SpongeVanillaMajorMinorMC(version, mcVersion) => version -> mcVersion
//                    }
//                )
//                    case "Forge" =>
//                        lowerBoundVersion.flatMap {
//                        //This will crash and burn if the implementation becomes
//                        //something else, but better that, than failing silently
//                        case version: DefaultArtifactVersion =>
//                            if (BigInt(version.getVersion.getFirstInteger) >= 28) {
//                                Some(version.toString) //Not sure what we really want to do here
//                            } else {
//                                version.toString match {
//                                    case OldForgeVersion(version) => Some(version)
//                                    case _                        => None
//                                }
//                            }
//                    } -> None //TODO
//                    case _ => None -> None
//                }
//            }
            promotedVersions.add(
                    new PromotedVersion(
                            version,
                            List.of(
                                    new PromotedVersionTag(
                                            tagName,
                                            data,
                                            data, // TODO
                                            null,
                                            color
                                    )
                            )
                    )
            );
        });
        return promotedVersions;
    }
    private String stringOrNull(@Nullable JsonNode string) {
        return string == null || string instanceof NullNode ? null : string.asText();
    }
}
