package io.papermc.hangar.components.auth.model;

import java.util.UUID;

public record PendingAccountDeletion(long id, UUID uuid, String name, String email) {
}
