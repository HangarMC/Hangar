package io.papermc.hangar.db.mappers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import io.papermc.hangar.db.customtypes.JSONB;
import io.papermc.hangar.model.generated.PromotedVersion;
import org.jdbi.v3.core.mapper.ColumnMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PromotedVersionMapper implements ColumnMapper<List<PromotedVersion>> {

    @Override
    public List<PromotedVersion> map(ResultSet rs, int column, StatementContext ctx) throws SQLException {
        JSONB jsonb = (JSONB) rs.getObject(column);
        JsonNode jsonNode = jsonb.getJson();
        ObjectMapper mapper = new ObjectMapper();
        ObjectReader reader = mapper.readerFor(new TypeReference<List<PromotedVersion>>() {
        });

        List<PromotedVersion> list = new ArrayList<>();
        try {
            list = reader.readValue(jsonNode);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }
}
