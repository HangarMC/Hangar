package io.papermc.hangar.model.api.project;

import io.papermc.hangar.model.common.roles.CompactRole;
import java.util.List;

public record ProjectMember(String user, long userId, List<CompactRole> roles) {
}
