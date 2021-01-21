package io.papermc.hangar.service;

import io.papermc.hangar.db.modelold.UsersTable;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.function.Supplier;

public abstract class HangarService {

    @Autowired
    protected Supplier<Optional<UsersTable>> currentUser;

    /**
     * Returns the user, or throws forbidden exception if not found
     *
     * @return current user
     */
    @NotNull
    public UsersTable getCurrentUser() {
        return currentUser.get().orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN));
    }
}
