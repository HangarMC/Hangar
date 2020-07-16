package me.minidigger.hangar.service;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import me.minidigger.hangar.model.generated.ModelData;
import me.minidigger.hangar.model.generated.User;

@Service
public class UserService {

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && !(authentication instanceof AnonymousAuthenticationToken)) {
            User user = new User();
            user.setName((String) authentication.getPrincipal());
            user.setAvatarUrl("https://paper.readthedocs.io/en/latest/_images/papermc_logomark_500.png");
            user.setId("dummyid");
            return user;
        }

        return null;
    }

    public ModelData getModelData() {
        ModelData modelData = new ModelData();

        return modelData;
    }
}
