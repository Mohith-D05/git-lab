package com.uniplanner.models;

import java.util.Objects;

/**
 * ========================================================================
 * OOP CONCEPTS: ENCAPSULATION + STATIC FACTORY METHOD + ENUM
 * ========================================================================
 * Represents a single scheduled class entry in the timetable.
 * Encapsulates: Faculty + Section + Subject + Room + TimeSlot
 *
 * - ENCAPSULATION: Immutable fields, controlled access
 * - STATIC FACTORY METHOD: createConflictSlot() for creating conflict entries
 *   without triggering validation (alternative to constructor)
 * - ENUM: ConflictStatus with descriptions
 * - COMPOSITION: Contains references to Section, Subject, Faculty, Space, TimeSlot
 * ========================================================================
 */
public class ScheduleSlot {
    private final String id;
    private final Section section;
    private final Subject subject;
    private final Faculty faculty;
    private final Space room;
    private final TimeSlot timeSlot;
    private final Space.ActivityType activityType;
    private ConflictStatus conflictStatus;
    private String conflictReason;

    /**
     * OOP CONCEPT: ENUM with behavior
     */
    public enum ConflictStatus {
        VALID("No conflicts"),
        FACULTY_CONFLICT("Faculty double-booking"),
        ROOM_CONFLICT("Room already occupied"),
        BOTH_CONFLICT("Faculty and room conflicts"),
        UNRESOLVED("Could not find available resources");

        private final String description;

        ConflictStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Standard constructor with validation.
     * Validates that the room can accommodate the activity type.
     */
    public ScheduleSlot(String id, Section section, Subject subject, Faculty faculty,
                       Space room, TimeSlot timeSlot, Space.ActivityType activityType) {
        this.id = Objects.requireNonNull(id, "ScheduleSlot ID cannot be null");
        this.section = Objects.requireNonNull(section, "Section cannot be null");
        this.subject = Objects.requireNonNull(subject, "Subject cannot be null");
        this.faculty = Objects.requireNonNull(faculty, "Faculty cannot be null");
        this.room = Objects.requireNonNull(room, "Room cannot be null");
        this.timeSlot = Objects.requireNonNull(timeSlot, "TimeSlot cannot be null");
        this.activityType = Objects.requireNonNull(activityType, "Activity type cannot be null");

        // Validate room can accommodate activity type
        if (!room.canAccommodateActivityType(activityType)) {
            throw new IllegalArgumentException(
                    String.format("Room %s cannot accommodate %s activity", room.getId(), activityType));
        }

        this.conflictStatus = ConflictStatus.VALID;
        this.conflictReason = "";
    }

    /**
     * ========================================================================
     * OOP CONCEPT: STATIC FACTORY METHOD
     * ========================================================================
     * Creates a conflict slot WITHOUT room-type validation.
     * Used by the scheduling algorithm when it cannot find proper resources
     * but still needs to track the conflict for the admin dashboard.
     *
     * This is preferred over making the constructor lenient because
     * normal slots SHOULD be validated — only conflict-tracking slots skip it.
     */
    public static ScheduleSlot createConflictSlot(String id, Section section, Subject subject,
                                                   Faculty faculty, Space room, TimeSlot timeSlot,
                                                   Space.ActivityType activityType, String reason) {
        ScheduleSlot slot = new ScheduleSlot();
        slot.conflictStatus = ConflictStatus.UNRESOLVED;
        slot.conflictReason = reason;
        // Use reflection-free direct field assignment via private constructor
        return new ScheduleSlot(id, section, subject, faculty, room, timeSlot, activityType, reason);
    }

    /**
     * Private constructor for conflict slots — bypasses room validation
     */
    private ScheduleSlot(String id, Section section, Subject subject, Faculty faculty,
                        Space room, TimeSlot timeSlot, Space.ActivityType activityType,
                        String conflictReason) {
        this.id = id;
        this.section = section;
        this.subject = subject;
        this.faculty = faculty;
        this.room = room;
        this.timeSlot = timeSlot;
        this.activityType = activityType;
        this.conflictStatus = ConflictStatus.UNRESOLVED;
        this.conflictReason = conflictReason;
    }

    /** Private no-arg constructor for factory use */
    private ScheduleSlot() {
        this.id = null;
        this.section = null;
        this.subject = null;
        this.faculty = null;
        this.room = null;
        this.timeSlot = null;
        this.activityType = null;
        this.conflictStatus = ConflictStatus.UNRESOLVED;
        this.conflictReason = "";
    }

    // ENCAPSULATION: Getters
    public String getId() {
        return id;
    }

    public Section getSection() {
        return section;
    }

    public Subject getSubject() {
        return subject;
    }

    public Faculty getFaculty() {
        return faculty;
    }

    public Space getRoom() {
        return room;
    }

    public TimeSlot getTimeSlot() {
        return timeSlot;
    }

    public Space.ActivityType getActivityType() {
        return activityType;
    }

    public ConflictStatus getConflictStatus() {
        return conflictStatus;
    }

    public String getConflictReason() {
        return conflictReason;
    }

    public void setConflictStatus(ConflictStatus status) {
        this.conflictStatus = Objects.requireNonNull(status, "Conflict status cannot be null");
    }

    /**
     * Check if this slot has any conflicts
     */
    public boolean hasConflicts() {
        return conflictStatus != ConflictStatus.VALID;
    }

    /**
     * Get activity duration in hours
     */
    public double getDurationHours() {
        return timeSlot != null ? (double) timeSlot.getDurationMinutes() / 60 : 0;
    }

    /**
     * Get a readable description of the slot
     */
    public String getScheduleDescription() {
        return String.format("%s | %s (%s) | %s | %s | Room: %s",
                section != null ? section.getSectionName() : "N/A",
                subject != null ? subject.getName() : "N/A",
                activityType,
                faculty != null ? faculty.getName() : "N/A",
                timeSlot != null ? timeSlot : "N/A",
                room != null ? room.getName() : "N/A");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScheduleSlot that = (ScheduleSlot) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        if (hasConflicts()) {
            return String.format("ScheduleSlot{CONFLICT: %s | %s}", conflictReason, getScheduleDescription());
        }
        return String.format("ScheduleSlot{%s}", getScheduleDescription());
    }
}
