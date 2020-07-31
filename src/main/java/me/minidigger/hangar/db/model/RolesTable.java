package me.minidigger.hangar.db.model;


import me.minidigger.hangar.db.customtypes.RoleCategory;
import me.minidigger.hangar.model.Role;

public class RolesTable {

    private long id;
    private String name;
    private RoleCategory category;
    private String title;
    private String color;
    private boolean isAssignable;
    private Long rank;
    private long permission;

    public static RolesTable fromRole(Role role) {
        return new RolesTable(role.getRoleId(), role.getValue(), role.getCategory(), role.getTitle(), role.getColor().getHex(), role.isAssignable(), role.getRank(), role.getPermissions().getValue());
    }

    public RolesTable() {
        //
    }

    public RolesTable(long id, String name, RoleCategory category, String title, String color, boolean isAssignable, Long rank, long permission) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.title = title;
        this.color = color;
        this.isAssignable = isAssignable;
        this.rank = rank;
        this.permission = permission;
    }

    public Role getRole() {
        return Role.fromTitle(title);
    }

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


    public Long getRank() {
        return rank;
    }

    public void setRank(Long rank) {
        this.rank = rank;
    }


    public long getPermission() {
        return permission;
    }

    public void setPermission(long permission) {
        this.permission = permission;
    }

}
