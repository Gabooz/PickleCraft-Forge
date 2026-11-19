package io.github.gabooz.picklecraft.farming;

import io.github.gabooz.picklecraft.seasons.Seasons;
import java.util.List;

/** Add future crop calendars here; windows use inclusive, one-based season days. */
public record CropSchedule(int growthDays, List<Window> windows) {
    public record Window(Seasons.Season season, int firstDay, int lastDay) {
        boolean contains(Seasons.Date date) { return date.season() == season && date.day() >= firstDay && date.day() <= lastDay; }
    }
    public static final CropSchedule WHEAT = new CropSchedule(15, List.of(
        new Window(Seasons.Season.SPRING, 1, 10), new Window(Seasons.Season.SUMMER, 1, 10),
        new Window(Seasons.Season.AUTUMN, 1, 5)));
    public CropSchedule { windows = List.copyOf(windows); }
    public boolean survives(Seasons.Date date) { return windows.stream().anyMatch(window -> window.contains(date)); }
    public boolean survivedInterval(long fromDay, long toDay) {
        // At most one full calendar year is needed to detect any lethal interval.
        for (long day = fromDay + 1; day <= Math.min(toDay, fromDay + Seasons.DAYS_PER_YEAR); day++) {
            if (!survives(Seasons.at(day * Seasons.DAY, 0))) return false;
        }
        return true;
    }
}
