package io.papermc.hangar.model.internal.sso;

import java.util.UUID;

public record SsoSyncData(String state, Traits traits, UUID id) {}
