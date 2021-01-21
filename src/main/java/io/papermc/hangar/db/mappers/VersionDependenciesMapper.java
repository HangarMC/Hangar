package io.papermc.hangar.db.mappers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.papermc.hangar.db.customtypes.JSONB;
import io.papermc.hangar.modelold.Platform;
import io.papermc.hangar.modelold.generated.Dependency;
import io.papermc.hangar.modelold.viewhelpers.VersionDependencies;
import org.jdbi.v3.core.mapper.ColumnMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class VersionDependenciesMapper implements ColumnMapper<VersionDependencies> {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public VersionDependencies map(ResultSet r, int columnNumber, StatementContext ctx) throws SQLException {
        ObjectNode objectNode = (ObjectNode) r.getObject(columnNumber, JSONB.class).getJson();

        VersionDependencies platformListMap = new VersionDependencies();
        objectNode.fields().forEachRemaining(stringJsonNodeEntry -> {
            ArrayNode depArray = (ArrayNode) stringJsonNodeEntry.getValue();
            List<Dependency> dependencies = new ArrayList<>();
            try {
                for (JsonNode depObj : depArray) {
                    dependencies.add(mapper.treeToValue(depObj, Dependency.class));
                }
            } catch (JsonProcessingException exception) {
                exception.printStackTrace();
            }

            platformListMap.put(Platform.valueOf(stringJsonNodeEntry.getKey()), dependencies);
        });

        return platformListMap;
    }
}
