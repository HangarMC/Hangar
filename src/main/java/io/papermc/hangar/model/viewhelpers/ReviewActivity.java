package io.papermc.hangar.model.viewhelpers;

import java.time.OffsetDateTime;

public class ReviewActivity extends Activity {

    private OffsetDateTime endedAt;
    public String versionString;

    public OffsetDateTime getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(OffsetDateTime endedAt) {
        this.endedAt = endedAt;
    }

    public String getVersionString() {
        return versionString;
    }

    public void setVersionString(String versionString) {
        this.versionString = versionString;
    }
}
