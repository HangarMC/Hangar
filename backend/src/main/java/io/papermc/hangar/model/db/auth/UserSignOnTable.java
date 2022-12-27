package io.papermc.hangar.model.db.auth;

import io.papermc.hangar.model.db.Table;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

import java.time.OffsetDateTime;

public class UserSignOnTable extends Table {

    private final String nonce;
    private boolean completed;

    @JdbiConstructor
    public UserSignOnTable(final OffsetDateTime createdAt, final long id, final String nonce, final boolean completed) {
        super(createdAt, id);
        this.nonce = nonce;
        this.completed = completed;
    }

    public UserSignOnTable(final String nonce) {
        this.nonce = nonce;
    }

    public String getNonce() {
        return this.nonce;
    }

    public boolean isCompleted() {
        return this.completed;
    }

    public void setCompleted(final boolean completed) {
        this.completed = completed;
    }

    @Override
    public String toString() {
        return "UserSignOnTable{" +
                "nonce='" + this.nonce + '\'' +
                ", completed=" + this.completed +
                "} " + super.toString();
    }
}
