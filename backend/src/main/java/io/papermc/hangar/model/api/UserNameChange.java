package io.papermc.hangar.model.api;

import java.time.OffsetDateTime;

public record UserNameChange(String oldName, String newName, OffsetDateTime date) {}
