package io.papermc.hangar.db.mappers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.papermc.hangar.model.api.project.version.FileInfo;
import io.papermc.hangar.model.api.project.version.PlatformVersionDownload;
import io.papermc.hangar.model.common.Platform;
import io.papermc.hangar.service.internal.file.FileService;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.EnumMap;
import java.util.Map;
import org.jdbi.v3.core.mapper.ColumnMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.springframework.stereotype.Component;

@Component
public class VersionDownloadsMapper implements ColumnMapper<Map<Platform, PlatformVersionDownload>> {

    private final ObjectMapper objectMapper;
    private final FileService fileService;

    public VersionDownloadsMapper(final ObjectMapper objectMapper, final FileService fileService) {
        this.objectMapper = objectMapper;
        this.fileService = fileService;
    }

    @Override
    public Map<Platform, PlatformVersionDownload> map(final ResultSet r, final int columnNumber, final StatementContext ctx) throws SQLException {
        final Map<Platform, PlatformVersionDownload> result = new EnumMap<>(Platform.class);
        final String raw = r.getString(columnNumber);

        if (raw == null) {
            return result;
        }

        final String[] projectNamespace = (String[]) r.getArray("project_namespace").getArray();
        final String version = r.getString("version_string");
        try {
            final JsonNode parsedDownloads = this.objectMapper.readTree(raw);
            for (final JsonNode download : parsedDownloads) {
                final JsonNode platforms = download.get("platforms");
                final String fileName = download.get("file_name").asText();
                final long fileSize = download.get("file_size").asLong();
                final String hash = download.get("hash").asText();
                final String externalUrl = download.get("external_url").asText();
                final FileInfo fileInfo = "null".equals(fileName) ? null : new FileInfo(fileName, fileSize, hash);
                final Platform downloadPlatform = Platform.values()[download.get("download_platform").asInt()];
                final String downloadUrl = fileInfo != null ? this.fileService.getVersionDownloadUrl(projectNamespace[0], projectNamespace[1], version, downloadPlatform, fileInfo.getName()) : null;
                final PlatformVersionDownload pvd = new PlatformVersionDownload(fileInfo, "null".equals(externalUrl) ? null : externalUrl, downloadUrl);
                for (final JsonNode platformId : platforms) {
                    result.put(Platform.values()[platformId.asInt()], pvd);
                }
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return result;
    }
}
