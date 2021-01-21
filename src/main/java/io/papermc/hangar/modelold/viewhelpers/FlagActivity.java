package io.papermc.hangar.modelold.viewhelpers;

import java.time.OffsetDateTime;

public class FlagActivity extends Activity {

    private OffsetDateTime resolvedAt;

    public OffsetDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(OffsetDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }
}
