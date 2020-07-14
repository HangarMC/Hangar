package me.minidigger.hangar.model;

public class Organization {

    private int id;
    private int userId;
    private String name;
    private int ownerId;

    public Organization(int id, int userId, String name, int ownerId) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.ownerId = ownerId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }
}
