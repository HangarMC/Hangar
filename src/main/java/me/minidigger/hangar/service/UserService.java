package me.minidigger.hangar.service;

import org.springframework.stereotype.Service;

import me.minidigger.hangar.model.ModelData;
import me.minidigger.hangar.model.User;

@Service
public class UserService {

    public User getCurrentUser() {
        User user = new User();
        user.setName("Test");
        user.setAvatarUrl("https://paper.readthedocs.io/en/latest/_images/papermc_logomark_500.png");
        user.setId("dummyid");
        return user;
    }

    public ModelData getModelData() {
        ModelData modelData = new ModelData();

        return modelData;
    }
}
