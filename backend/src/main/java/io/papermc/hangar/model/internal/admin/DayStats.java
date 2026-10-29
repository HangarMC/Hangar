package io.papermc.hangar.model.internal.admin;

import java.time.LocalDate;

public record DayStats(LocalDate day, long reviews, long uploads, long downloads, long views, long newProjects, long newUsers, long flagsOpened, long flagsClosed) {
}
