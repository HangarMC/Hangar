package me.minidigger.hangar.service;

import org.springframework.stereotype.Service;

import java.util.List;

import me.minidigger.hangar.model.generated.Organization;
import me.minidigger.hangar.model.Permission;
import me.minidigger.hangar.model.generated.User;

@Service
public class OrgService {

    public List<Organization> getOrgsWithPerm(User user, Permission permission) {
        return List.of(new Organization(-1, -1, "TestOrg", -1));
    }
}
