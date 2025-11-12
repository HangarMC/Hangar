package io.papermc.hangar.components.health;

import io.papermc.hangar.db.customtypes.JSONB;
import java.time.OffsetDateTime;
import java.util.Objects;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

public class HealthReportTable {

    private long id;
    private JSONB report;
    private String queuedBy;
    private OffsetDateTime queuedAt;
    private OffsetDateTime finishedAt;
    private String status;

    @JdbiConstructor
    public HealthReportTable(final long id, final JSONB report, final String queuedBy, final OffsetDateTime queuedAt, final OffsetDateTime finishedAt, final String status) {
        this.id = id;
        this.report = report;
        this.queuedBy = queuedBy;
        this.queuedAt = queuedAt;
        this.finishedAt = finishedAt;
        this.status = status;
    }

    public HealthReportTable(final String queuedBy) {
        this.queuedBy = queuedBy;
        this.queuedAt = OffsetDateTime.now();
        this.status = "queued";
    }

    public long getId() {
        return this.id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public JSONB getReport() {
        return this.report;
    }

    public void setReport(final JSONB report) {
        this.report = report;
    }

    public String getQueuedBy() {
        return this.queuedBy;
    }

    public void setQueuedBy(final String queuedBy) {
        this.queuedBy = queuedBy;
    }

    public OffsetDateTime getQueuedAt() {
        return this.queuedAt;
    }

    public void setQueuedAt(final OffsetDateTime queuedAt) {
        this.queuedAt = queuedAt;
    }

    public OffsetDateTime getFinishedAt() {
        return this.finishedAt;
    }

    public void setFinishedAt(final OffsetDateTime finishedAt) {
        this.finishedAt = finishedAt;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(final String status) {
        this.status = status;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null || this.getClass() != o.getClass()) return false;
        final HealthReportTable that = (HealthReportTable) o;
        return this.id == that.id && Objects.equals(this.report, that.report) && Objects.equals(this.queuedBy, that.queuedBy) && Objects.equals(this.queuedAt, that.queuedAt) && Objects.equals(this.finishedAt, that.finishedAt) && Objects.equals(this.status, that.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id, this.report, this.queuedBy, this.queuedAt, this.finishedAt, this.status);
    }
}
