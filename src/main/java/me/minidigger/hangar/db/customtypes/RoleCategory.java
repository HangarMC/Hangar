package me.minidigger.hangar.db.customtypes;

public enum RoleCategory {
    //TODO implement RoleCategory type

    GLOBAL("global"),
    PROJECT("project"),
    ORGANIZATION("organization");

    private String value;

    RoleCategory(String value) {
        this.value = value;
    }
}
