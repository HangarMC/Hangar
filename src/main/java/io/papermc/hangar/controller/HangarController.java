package io.papermc.hangar.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.function.Supplier;

import io.papermc.hangar.db.modelold.UsersTable;

public abstract class HangarController {
    @Autowired
    private Supplier<Optional<UsersTable>> currentUser;

    protected UsersTable getCurrentUser() {
        return currentUser.get().orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }
}
