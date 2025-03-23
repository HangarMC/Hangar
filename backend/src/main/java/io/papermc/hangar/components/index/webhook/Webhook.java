package io.papermc.hangar.components.index.webhook;

import java.time.Duration;
import java.time.LocalDateTime;

public record Webhook(
    String uid,
    String indexUid,
    String status,
    String type,
    String canceledBy,
    Details details,
    Duration duration,
    String enqueuedAt,
    LocalDateTime startedAt,
    LocalDateTime finishedAt,
    Error error
) {
    record Details(int receivedDocuments, int indexedDocuments) {
    }

    record Error(String message, String code, String type, String link) {
    }
}
