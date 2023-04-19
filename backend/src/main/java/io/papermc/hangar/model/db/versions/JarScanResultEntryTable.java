package io.papermc.hangar.model.db.versions;

import io.papermc.hangar.model.db.Table;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

public class JarScanResultEntryTable extends Table {

    private final long resultId;
    private final String location;
    private final String message;
    private final String severity;

    @JdbiConstructor
    public JarScanResultEntryTable(final long id, final long resultId, final String location, final String message, final String severity) {
        super(id);
        this.resultId = resultId;
        this.location = location;
        this.message = message;
        this.severity = severity;
    }

    public JarScanResultEntryTable(final long resultId, final String location, final String message, final String severity) {
        this.resultId = resultId;
        this.location = location;
        this.message = message;
        this.severity = severity;
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
}
