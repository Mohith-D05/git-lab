package com.uniplanner.models;

import java.util.*;

/**
 * ========================================================================
 * OOP CONCEPTS: ENCAPSULATION + INTERFACE IMPLEMENTATION + COMPARABLE
 * ========================================================================
 * Faculty member with expertise tracking and workload management.
 *
 * - ENCAPSULATION: Private fields, public getters, controlled mutation
 * - INTERFACE: Implements Schedulable (common contract with Space)
 * - COMPARABLE: Implements Comparable for workload-based sorting
 * - ENUM: WorkloadStatus (GREEN/AMBER/RED) with behavior
 * - COLLECTIONS: Uses Set<String> for expertise, List<String> for assistants
 * ========================================================================
 */
public class Faculty implements Schedulable, Comparable<Faculty> {

    // ENCAPSULATION: Private fields — no direct external access
    private final String id;
    private final String name;
    private final String email;
    private final String department;
    private final Set<String> expertiseAreas;
    private final int maxHoursPerWeek;
    private int assignedHoursThisWeek;
    private final List<String> associatedLabAssistants;
    private WorkloadStatus workloadStatus;

    /**
     * OOP CONCEPT: ENUM with behavior — each status has a description
     */
    public enum WorkloadStatus {
        GREEN("Available - Low workload"),
        AMBER("Moderate - Medium workload"),
        RED("Busy - High workload");

        private final String description;

        WorkloadStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // ENCAPSULATION: Constructor with input validation
    public Faculty(String id, String name, String email, String department, int maxHoursPerWeek) {
        this.id = Objects.requireNonNull(id, "Faculty ID cannot be null");
        this.name = Objects.requireNonNull(name, "Faculty name cannot be null");
        this.email = Objects.requireNonNull(email, "Email cannot be null");
        this.department = Objects.requireNonNull(department, "Department cannot be null");

        if (maxHoursPerWeek <= 0 || maxHoursPerWeek > 50) {
            throw new IllegalArgumentException("Max hours must be between 1 and 50");
        }

        this.maxHoursPerWeek = maxHoursPerWeek;
        this.assignedHoursThisWeek = 0;
        this.expertiseAreas = new HashSet<>();
        this.associatedLabAssistants = new ArrayList<>();
        this.workloadStatus = WorkloadStatus.GREEN;
    }

    // ========================================================================
    // INTERFACE IMPLEMENTATION: Schedulable
    // ========================================================================

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isAvailable() {
        return workloadStatus != WorkloadStatus.RED && getRemainingCapacity() > 0;
    }

    @Override
    public String getStatusDescription() {
        return String.format("%s [%s] — %d/%d hours (%s)",
                name, department, assignedHoursThisWeek, maxHoursPerWeek,
                workloadStatus.getDescription());
    }

    // ========================================================================
    // INTERFACE IMPLEMENTATION: Comparable (sort by workload for optimization)
    // ========================================================================

    @Override
    public int compareTo(Faculty other) {
        return Integer.compare(this.assignedHoursThisWeek, other.assignedHoursThisWeek);
    }

    // ENCAPSULATION: Getters
    public String getEmail() {
        return email;
    }

    public String getDepartment() {
        return department;
    }

    public int getMaxHoursPerWeek() {
        return maxHoursPerWeek;
    }

    public int getAssignedHoursThisWeek() {
        return assignedHoursThisWeek;
    }

    public Set<String> getExpertiseAreas() {
        // ENCAPSULATION: Return unmodifiable view — caller cannot modify internal set
        return Collections.unmodifiableSet(expertiseAreas);
    }

    public List<String> getAssociatedLabAssistants() {
        return Collections.unmodifiableList(associatedLabAssistants);
    }

    public WorkloadStatus getWorkloadStatus() {
        return workloadStatus;
    }

    /**
     * Add expertise area
     */
    public void addExpertise(String expertise) {
        expertiseAreas.add(Objects.requireNonNull(expertise, "Expertise cannot be null"));
    }

    /**
     * Remove expertise area
     */
    public void removeExpertise(String expertise) {
        expertiseAreas.remove(expertise);
    }

    /**
     * Check if faculty has expertise in a subject area
     * Uses case-insensitive partial matching for flexibility
     */
    public boolean hasExpertise(String expertise) {
        if (expertise == null) {
            return false;
        }

        String normalizedExpertise = expertise.trim().toLowerCase();
        return expertiseAreas.stream()
                .map(area -> area == null ? "" : area.trim().toLowerCase())
                .anyMatch(area -> area.equals(normalizedExpertise)
                        || area.contains(normalizedExpertise)
                        || normalizedExpertise.contains(area));
    }

    /**
     * Add lab assistant association (soft constraint)
     */
    public void associateLabAssistant(String assistantId) {
        if (!associatedLabAssistants.contains(assistantId)) {
            associatedLabAssistants.add(assistantId);
        }
    }

    /**
     * Check if faculty can take additional hours (hard constraint)
     */
    public boolean canTakeHours(int hours) {
        return (assignedHoursThisWeek + hours) <= maxHoursPerWeek;
    }

    /**
     * Assign hours to faculty — updates workload status automatically
     * ENCAPSULATION: Controlled mutation with validation
     */
    public void assignHours(int hours) {
        if (!canTakeHours(hours)) {
            throw new IllegalArgumentException("Hours exceed maximum capacity");
        }
        this.assignedHoursThisWeek += hours;
        updateWorkloadStatus();
    }

    /**
     * Release assigned hours
     */
    public void releaseHours(int hours) {
        this.assignedHoursThisWeek = Math.max(0, assignedHoursThisWeek - hours);
        updateWorkloadStatus();
    }

    /**
     * Calculate remaining capacity
     */
    public int getRemainingCapacity() {
        return maxHoursPerWeek - assignedHoursThisWeek;
    }

    /**
     * Get workload percentage
     */
    public double getWorkloadPercentage() {
        return (double) assignedHoursThisWeek / maxHoursPerWeek * 100;
    }

    /**
     * ENCAPSULATION: Private method — internal state management
     * Updates the workload status indicator (Green/Amber/Red)
     */
    private void updateWorkloadStatus() {
        double percentage = getWorkloadPercentage();
        if (percentage <= 50) {
            workloadStatus = WorkloadStatus.GREEN;
        } else if (percentage <= 80) {
            workloadStatus = WorkloadStatus.AMBER;
        } else {
            workloadStatus = WorkloadStatus.RED;
        }
    }

    /**
     * Reset weekly assignment (call at start of new timetable generation)
     */
    public void resetWeeklyAssignment() {
        assignedHoursThisWeek = 0;
        updateWorkloadStatus();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Faculty faculty = (Faculty) o;
        return id.equals(faculty.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("Faculty{id='%s', name='%s', hours=%d/%d (%s), expertise=%s}",
                id, name, assignedHoursThisWeek, maxHoursPerWeek, workloadStatus, expertiseAreas);
    }
}
