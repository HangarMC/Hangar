package me.minidigger.hangar.service;

import org.springframework.stereotype.Service;

import java.util.List;

import me.minidigger.hangar.db.model.UsersTable;
import me.minidigger.hangar.model.Permission;
import me.minidigger.hangar.model.generated.Organization;

@Service
public class OrgService {

    public List<Organization> getOrgsWithPerm(UsersTable user, Permission permission) {
        return List.of(new Organization(-1, -1, "TestOrg", -1));
    }
}
