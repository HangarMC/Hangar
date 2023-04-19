package io.papermc.hangar.model.internal.admin;

import java.time.LocalDate;

public record DayStats(LocalDate day, long reviews, long uploads, long totalDownloads, long flagsOpened, long flagsClosed) {
}
