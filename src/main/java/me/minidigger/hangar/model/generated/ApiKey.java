package me.minidigger.hangar.model.generated;

import me.minidigger.hangar.db.model.UsersTable;
import me.minidigger.hangar.model.Permission;

public class ApiKey {

    private String name;
    private UsersTable owner;
    private String tokenIdentifier;
    private Permission permission;
}
