package io.papermc.hangar.db.mappers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import io.papermc.hangar.db.customtypes.JSONB;
import io.papermc.hangar.model.generated.PromotedVersion;
import org.jdbi.v3.core.mapper.ColumnMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PromotedVersionMapper implements ColumnMapper<List<PromotedVersion>> {
    public List<PromotedVersion> map(ResultSet r, int columnNumber, StatementContext ctx) throws SQLException {
        JSONB jsonb = (JSONB) r.getObject(columnNumber);
        LoggerFactory.getLogger(PromotedVersionMapper.class).info(jsonb.toString()); // for json logging
        JsonNode jsonNode = jsonb.getJson();
        ObjectMapper mapper = new ObjectMapper();
        ObjectReader reader = mapper.readerFor(new TypeReference<List<PromotedVersion>>() {});
        List<PromotedVersion> list = new ArrayList<>();
        try {
            list = reader.readValue(jsonNode);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }
}