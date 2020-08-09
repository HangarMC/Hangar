package me.minidigger.hangar.db.model;


import java.time.OffsetDateTime;

public class UserSignOnsTable {

    private long id;
    private OffsetDateTime createdAt;
    private String nonce;
    private boolean isCompleted;

    public UserSignOnsTable(String nonce) {
        this.nonce = nonce;
    }

    public UserSignOnsTable() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }


    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }


    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }


    public boolean getIsCompleted() {
        return isCompleted;
    }

    public void setIsCompleted(boolean isCompleted) {
        this.isCompleted = isCompleted;
    }

}
