package io.papermc.hangar.model.internal.logs.viewmodels;

import org.jdbi.v3.core.mapper.PropagateNull;

public record LogPage(@PropagateNull Long id, String name, String slug) {
}
