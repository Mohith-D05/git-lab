package com.uniplanner.models;

import java.time.LocalTime;
import java.util.Objects;

/**
 * Represents a time slot (hour block) available for scheduling
 * Immutable value object for time-based constraints
 */
public class TimeSlot {
    private final DayOfWeek day;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final int durationMinutes;

    public enum DayOfWeek {
        MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY
    }

    public TimeSlot(DayOfWeek day, LocalTime startTime, LocalTime endTime) {
        this.day = Objects.requireNonNull(day, "Day cannot be null");
        this.startTime = Objects.requireNonNull(startTime, "Start time cannot be null");
        this.endTime = Objects.requireNonNull(endTime, "End time cannot be null");

        if (!startTime.isBefore(endTime)) {
            throw new IllegalArgumentException("Start time must be before end time");
        }

        this.durationMinutes = (int) java.time.temporal.ChronoUnit.MINUTES.between(startTime, endTime);
    }

    // Getters (encapsulation - read-only)
    public DayOfWeek getDay() {
        return day;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    /**
     * Checks if this time slot overlaps with another
     */
    public boolean overlaps(TimeSlot other) {
        if (!this.day.equals(other.day)) {
            return false;
        }
        return this.startTime.isBefore(other.endTime) && other.startTime.isBefore(this.endTime);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimeSlot timeSlot = (TimeSlot) o;
        return day == timeSlot.day &&
                startTime.equals(timeSlot.startTime) &&
                endTime.equals(timeSlot.endTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(day, startTime, endTime);
    }

    @Override
    public String toString() {
        return String.format("%s %s-%s", day, startTime, endTime);
    }
}
