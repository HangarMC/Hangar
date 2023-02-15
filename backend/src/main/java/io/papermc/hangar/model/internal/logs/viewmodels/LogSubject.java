package io.papermc.hangar.model.internal.logs.viewmodels;

import org.jdbi.v3.core.mapper.PropagateNull;

/**
 * Users AND Organizations
 */
public record LogSubject(@PropagateNull Long id, String name) {
}
