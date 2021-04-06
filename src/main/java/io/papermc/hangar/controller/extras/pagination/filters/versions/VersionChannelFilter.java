package io.papermc.hangar.controller.extras.pagination.filters.versions;

import io.papermc.hangar.controller.extras.pagination.Filter;
import io.papermc.hangar.controller.extras.pagination.filters.versions.VersionChannelFilter.VersionChannelFilterInstance;
import org.jdbi.v3.core.statement.SqlStatement;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.NativeWebRequest;

import java.util.Arrays;

@Component
public class VersionChannelFilter implements Filter<VersionChannelFilterInstance> {

    @Override
    public String getQueryParamName() {
        return "channel";
    }

    @Override
    public String getDescription() {
        return "A name of a version channel to filter for";
    }

    @NotNull
    @Override
    public VersionChannelFilterInstance create(NativeWebRequest webRequest) {
        return new VersionChannelFilterInstance(webRequest.getParameterValues(getQueryParamName()));
    }

    static class VersionChannelFilterInstance implements FilterInstance {

        private final String[] channels;

        VersionChannelFilterInstance(String[] channels) {
            this.channels = channels;
        }

        @Override
        public void createSql(StringBuilder sb, SqlStatement<?> q) {
            sb.append(" AND pc.name IN (");
            for (int i = 0; i < channels.length; i++) {
                sb.append(":__channelName_").append(i);
                q.bind("__channelName_" + i, channels[i]);
                if (i + 1 != channels.length) {
                    sb.append(",");
                }
            }
            sb.append(")");
        }

        @Override
        public String toString() {
            return "VersionChannelFilterInstance{" +
                    "channels=" + Arrays.toString(channels) +
                    '}';
        }
    }
}
