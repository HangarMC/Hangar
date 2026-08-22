package io.papermc.hangar.model.db.versions;

import io.papermc.hangar.model.db.Table;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

public class JarScanResultEntryTable extends Table {

    private final long resultId;
    private final String location;
    private final String message;
    private final String severity;
    private final String checkName;

    @JdbiConstructor
    public JarScanResultEntryTable(final long id, final long resultId, final String location, final String message, final String severity, final String checkName) {
        super(id);
        this.resultId = resultId;
        this.location = location;
        this.message = message;
        this.severity = severity;
        this.checkName = checkName;
    }

    public JarScanResultEntryTable(final long resultId, final String location, final String message, final String severity, final String checkName) {
        this.resultId = resultId;
        this.location = location;
        this.message = message;
        this.severity = severity;
        this.checkName = checkName;
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
}
