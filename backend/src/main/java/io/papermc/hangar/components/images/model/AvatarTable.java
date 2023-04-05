package io.papermc.hangar.components.images.model;

import io.papermc.hangar.model.db.Table;
import java.time.OffsetDateTime;
import java.util.StringJoiner;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

public class AvatarTable extends Table {

    private String type;
    private String subject;
    private String optimizedHash;
    private String unoptimizedHash;
    private int version;

    @JdbiConstructor
    public AvatarTable(final long id, final OffsetDateTime createdAt, final String type, final String subject, final String optimizedHash, final String unoptimizedHash, final int version) {
        super(createdAt, id);
        this.type = type;
        this.subject = subject;
        this.optimizedHash = optimizedHash;
        this.unoptimizedHash = unoptimizedHash;
        this.version = version;
    }

    public AvatarTable(final String type, final String subject, final String optimizedHash, final String unoptimizedHash, final int version) {
        this.type = type;
        this.subject = subject;
        this.optimizedHash = optimizedHash;
        this.unoptimizedHash = unoptimizedHash;
        this.version = version;
    }

    public String getType() {
        return this.type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public String getSubject() {
        return this.subject;
    }

    public void setSubject(final String subject) {
        this.subject = subject;
    }

    public String getOptimizedHash() {
        return this.optimizedHash;
    }

    public void setOptimizedHash(final String optimizedHash) {
        this.optimizedHash = optimizedHash;
    }

    public String getUnoptimizedHash() {
        return this.unoptimizedHash;
    }

    public void setUnoptimizedHash(final String unoptimizedHash) {
        this.unoptimizedHash = unoptimizedHash;
    }

    public int getVersion() {
        return this.version;
    }

    public void setVersion(final int version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", AvatarTable.class.getSimpleName() + "[", "]")
            .add("type='" + this.type + "'")
            .add("subject='" + this.subject + "'")
            .add("optimizedHash='" + this.optimizedHash + "'")
            .add("unoptimizedHash='" + this.unoptimizedHash + "'")
            .add("version='" + this.version + "'")
            .toString();
    }
}
