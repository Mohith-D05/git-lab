package com.uniplanner.models;

import java.util.*;
import java.util.stream.Collectors;

/**
 * ========================================================================
 * OOP CONCEPTS: COMPOSITION + COLLECTIONS + ENCAPSULATION
 * ========================================================================
 * Represents a complete timetable for all sections.
 * Contains all scheduled slots and tracks conflicts and statistics.
 *
 * - COMPOSITION: Contains lists of ScheduleSlot objects
 * - ENCAPSULATION: Private mutable state with controlled access
 * - COLLECTIONS: Uses ArrayList, HashMap, LinkedHashMap, Optional
 * - STREAMS API: Functional filtering and aggregation
 * - ENUM: ConflictType for categorization
 * ========================================================================
 */
public class Timetable {
    private final String id;
    private final String departmentName;
    private final String semester;
    private final List<ScheduleSlot> scheduledSlots;
    private final List<ScheduleSlot> conflictingSlots;
    private final Map<String, Integer> conflictCountByType;
    private boolean isGenerated;

    public enum ConflictType {
        FACULTY_DOUBLE_BOOKING,
        ROOM_OVERLAP,
        STUDENT_CLASH,
        OTHER
    }

    public Timetable(String id, String departmentName, String semester) {
        this.id = Objects.requireNonNull(id, "Timetable ID cannot be null");
        this.departmentName = Objects.requireNonNull(departmentName, "Department name cannot be null");
        this.semester = Objects.requireNonNull(semester, "Semester cannot be null");
        this.scheduledSlots = new ArrayList<>();
        this.conflictingSlots = new ArrayList<>();
        this.conflictCountByType = new HashMap<>();
        this.isGenerated = false;
    }

    // Getters (encapsulation)
    public String getId() {
        return id;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public String getSemester() {
        return semester;
    }

    public List<ScheduleSlot> getScheduledSlots() {
        return Collections.unmodifiableList(scheduledSlots);
    }

    public List<ScheduleSlot> getConflictingSlots() {
        return Collections.unmodifiableList(conflictingSlots);
    }

    public boolean isGenerated() {
        return isGenerated;
    }

    /**
     * Add a scheduled slot to the timetable
     */
    public void addScheduleSlot(ScheduleSlot slot) {
        scheduledSlots.add(Objects.requireNonNull(slot, "ScheduleSlot cannot be null"));

        if (slot.hasConflicts()) {
            conflictingSlots.add(slot);
            String conflictType = slot.getConflictStatus().toString();
            conflictCountByType.put(conflictType,
                    conflictCountByType.getOrDefault(conflictType, 0) + 1);
        }
    }

    /**
     * Remove a scheduled slot from the timetable
     */
    public void removeScheduleSlot(String slotId) {
        Optional<ScheduleSlot> slot = scheduledSlots.stream()
                .filter(s -> s.getId().equals(slotId))
                .findFirst();

        if (slot.isPresent()) {
            scheduledSlots.remove(slot.get());
            if (slot.get().hasConflicts()) {
                conflictingSlots.remove(slot.get());
                String conflictType = slot.get().getConflictStatus().toString();
                Integer count = conflictCountByType.get(conflictType);
                if (count != null && count > 1) {
                    conflictCountByType.put(conflictType, count - 1);
                } else {
                    conflictCountByType.remove(conflictType);
                }
            }
        }
    }

    /**
     * Get slot by ID
     */
    public Optional<ScheduleSlot> getSlotById(String slotId) {
        return scheduledSlots.stream()
                .filter(s -> s.getId().equals(slotId))
                .findFirst();
    }

    /**
     * Get all slots for a specific section (null-safe for conflict slots)
     */
    public List<ScheduleSlot> getSlotsBySection(String sectionId) {
        return scheduledSlots.stream()
                .filter(slot -> slot.getSection() != null && slot.getSection().getId().equals(sectionId))
                .collect(Collectors.toList());
    }

    /**
     * Get all slots for a specific faculty (null-safe for conflict slots)
     */
    public List<ScheduleSlot> getSlotsByFaculty(String facultyId) {
        return scheduledSlots.stream()
                .filter(slot -> slot.getFaculty() != null && slot.getFaculty().getId().equals(facultyId))
                .collect(Collectors.toList());
    }

    /**
     * Get all slots in a specific room (null-safe for conflict slots)
     */
    public List<ScheduleSlot> getSlotsByRoom(String roomId) {
        return scheduledSlots.stream()
                .filter(slot -> slot.getRoom() != null && slot.getRoom().getId().equals(roomId))
                .collect(Collectors.toList());
    }

    /**
     * Get total number of conflicts
     */
    public int getTotalConflictCount() {
        return conflictingSlots.size();
    }

    /**
     * Get conflicts by type
     */
    public Map<String, Integer> getConflictCountByType() {
        return Collections.unmodifiableMap(conflictCountByType);
    }

    /**
     * Check if timetable is valid (no conflicts)
     */
    public boolean isValid() {
        return conflictingSlots.isEmpty();
    }

    /**
     * Get timetable statistics
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalSlots", scheduledSlots.size());
        stats.put("conflictingSlots", conflictingSlots.size());
        stats.put("conflictPercentage", scheduledSlots.isEmpty() ? 0 :
                (double) conflictingSlots.size() / scheduledSlots.size() * 100);
        stats.put("isValid", isValid());
        stats.put("departmentName", departmentName);
        stats.put("semester", semester);
        return stats;
    }

    /**
     * Mark timetable as generated/finalized
     */
    public void markAsGenerated() {
        this.isGenerated = true;
    }

    /**
     * Reset the timetable (clear all slots)
     */
    public void reset() {
        scheduledSlots.clear();
        conflictingSlots.clear();
        conflictCountByType.clear();
        isGenerated = false;
    }

    /**
     * Get readable timetable summary
     */
    public String getSummary() {
        return String.format("Timetable{id='%s', department='%s', semester='%s', slots=%d, conflicts=%d}",
                id, departmentName, semester, scheduledSlots.size(), conflictingSlots.size());
    }

    @Override
    public String toString() {
        return getSummary();
    }
}
