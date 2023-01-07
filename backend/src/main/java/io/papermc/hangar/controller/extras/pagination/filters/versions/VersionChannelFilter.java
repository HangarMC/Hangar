package io.papermc.hangar.controller.extras.pagination.filters.versions;

import io.papermc.hangar.controller.extras.pagination.Filter;
import io.papermc.hangar.controller.extras.pagination.filters.versions.VersionChannelFilter.VersionChannelFilterInstance;
import java.util.Arrays;
import java.util.Set;
import org.jdbi.v3.core.statement.SqlStatement;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.NativeWebRequest;

@Component
public class VersionChannelFilter implements Filter<VersionChannelFilterInstance, String[]> {

    @Override
    public Set<String> getQueryParamNames() {
        return Set.of("channel");
    }

    @Override
    public String getDescription() {
        return "A name of a version channel to filter for";
    }

    @Override
    public String[] getValue(final NativeWebRequest webRequest) {
        return webRequest.getParameterValues(this.getSingleQueryParam());
    }

    @Override
    public @NotNull VersionChannelFilterInstance create(final NativeWebRequest webRequest) {
        return new VersionChannelFilterInstance(this.getValue(webRequest));
    }

    public static class VersionChannelFilterInstance implements Filter.FilterInstance {

        private final String[] channels;

        public VersionChannelFilterInstance(final String[] channels) {
            this.channels = channels;
        }

        @Override
        public void createSql(final StringBuilder sb, final SqlStatement<?> q) {
            sb.append(" AND pc.name IN (");
            for (int i = 0; i < this.channels.length; i++) {
                sb.append(":__channelName_").append(i);
                q.bind("__channelName_" + i, this.channels[i]);
                if (i + 1 != this.channels.length) {
                    sb.append(",");
                }
            }
            sb.append(")");
        }

        @Override
        public String toString() {
            return "VersionChannelFilterInstance{" +
                "channels=" + Arrays.toString(this.channels) +
                '}';
        }
    }
}
