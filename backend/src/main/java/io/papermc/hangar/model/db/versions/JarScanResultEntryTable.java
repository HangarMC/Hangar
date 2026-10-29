package io.papermc.hangar.model.db.versions;

import io.papermc.hangar.model.db.Table;
import java.time.OffsetDateTime;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

public class JarScanResultEntryTable extends Table {

    private final long resultId;
    private final String location;
    private final String message;
    private final String severity;
    private final String checkName;
    private final String hash;
    private boolean checked;
    private OffsetDateTime checkedAt;
    private Long checkedBy;

    @JdbiConstructor
    public JarScanResultEntryTable(final long id, final long resultId, final String location, final String message, final String severity, final String checkName, final String hash, final boolean checked, final OffsetDateTime checkedAt, final Long checkedBy) {
        super(id);
        this.resultId = resultId;
        this.location = location;
        this.message = message;
        this.severity = severity;
        this.checkName = checkName;
        this.hash = hash;
        this.checked = checked;
        this.checkedAt = checkedAt;
        this.checkedBy = checkedBy;
    }

    public JarScanResultEntryTable(final long resultId, final String location, final String message, final String severity, final String checkName, final String hash, final boolean checked, final OffsetDateTime checkedAt, final Long checkedBy) {
        this.resultId = resultId;
        this.location = location;
        this.message = message;
        this.severity = severity;
        this.checkName = checkName;
        this.hash = hash;
        this.checked = checked;
        this.checkedAt = checkedAt;
        this.checkedBy = checkedBy;
    }

    public long getResultId() {
        return this.resultId;
    }

    public String getLocation() {
        return this.location;
    }

    public String getMessage() {
        return this.message;
    }

    public String getSeverity() {
        return this.severity;
    }

    public String getCheckName() {
        return this.checkName;
    }

    public String getHash() {
        return this.hash;
    }

    public boolean isChecked() {
        return this.checked;
    }

    public void setChecked(final boolean checked) {
        this.checked = checked;
    }

    public OffsetDateTime getCheckedAt() {
        return this.checkedAt;
    }

    public void setCheckedAt(final OffsetDateTime checkedAt) {
        this.checkedAt = checkedAt;
    }

    public Long getCheckedBy() {
        return this.checkedBy;
    }

    public void setCheckedBy(final Long checkedBy) {
        this.checkedBy = checkedBy;
    }
}
