package me.minidigger.hangar.service;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import me.minidigger.hangar.db.model.UsersTable;
import me.minidigger.hangar.model.generated.ModelData;
import me.minidigger.hangar.security.HangarAuthentication;

@Service
public class UserService {

    public UsersTable getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && !(authentication instanceof AnonymousAuthenticationToken)) {
            HangarAuthentication auth = (HangarAuthentication) authentication;
//            User user = new User();
//            user.setName((String) authentication.getPrincipal());
//            user.setAvatarUrl("https://paper.readthedocs.io/en/latest/_images/papermc_logomark_500.png");
//            user.setId("dummyid");
            return auth.getTable();
        }

        return null;
    }

    public ModelData getModelData() {
        ModelData modelData = new ModelData();

        return modelData;
    }
}
