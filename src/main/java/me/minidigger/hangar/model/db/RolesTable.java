package me.minidigger.hangar.model.db;


import me.minidigger.hangar.model.db.customtypes.RoleCategory;

public class RolesTable {

    private long id;
    private String name;
    private RoleCategory category;
    private String title;
    private String color;
    private boolean isAssignable;
    private long rank;
    private long permission;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public RoleCategory getCategory() {
        return category;
    }

    public void setCategory(RoleCategory category) {
        this.category = category;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }


    public boolean getIsAssignable() {
        return isAssignable;
    }

    public void setIsAssignable(boolean isAssignable) {
        this.isAssignable = isAssignable;
    }


    public long getRank() {
        return rank;
    }

    public void setRank(long rank) {
        this.rank = rank;
    }


    public long getPermission() {
        return permission;
    }

    public void setPermission(long permission) {
        this.permission = permission;
    }

}
