package io.papermc.hangar.db.mappers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import io.papermc.hangar.db.customtypes.JSONB;
import io.papermc.hangar.model.generated.PlatformDependency;
import org.jdbi.v3.core.mapper.ColumnMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PlatformDependencyMapper implements ColumnMapper<List<PlatformDependency>> {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public List<PlatformDependency> map(ResultSet r, int columnNumber, StatementContext ctx) throws SQLException {
        ArrayNode platformDepsArray = (ArrayNode) r.getObject(columnNumber, JSONB.class).getJson();
        List<PlatformDependency> platformDependencies = new ArrayList<>();
        try {
            for (JsonNode platformDepObj : platformDepsArray) {
                platformDependencies.add(mapper.treeToValue(platformDepObj, PlatformDependency.class));
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return platformDependencies;
    }
}
