package com.uniplanner.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * ========================================================================
 * OOP CONCEPT: ABSTRACTION (Abstract Class)
 * ========================================================================
 * Abstract base class for all physical spaces/rooms in the university.
 * Cannot be instantiated directly — must use concrete subclasses
 * LectureHall or ComputerLab.
 *
 * Also demonstrates:
 * - INTERFACE IMPLEMENTATION: Implements Schedulable and Comparable
 * - ENCAPSULATION: Protected/private fields with public getters
 * - POLYMORPHISM: Abstract methods overridden by subclasses
 * - ENUM: Nested RoomStatus and ActivityType enums
 * ========================================================================
 */
public abstract class Space implements Schedulable, Comparable<Space> {

    // ENCAPSULATION: protected fields accessible to subclasses only
    protected final String id;
    protected final String name;
    protected final int capacity;
    protected RoomStatus status;
    protected final List<TimeSlot> blockedTimeSlots;

    /**
     * OOP CONCEPT: ENUM — Type-safe constants with behavior
     */
    public enum RoomStatus {
        ACTIVE("Active"),
        MAINTENANCE("Maintenance");

        private final String displayName;

        RoomStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * OOP CONCEPT: ENUM — Activity types for scheduling constraints
     */
    public enum ActivityType {
        LECTURE,
        TUTORIAL,
        PRACTICAL
    }

    // ENCAPSULATION: Constructor validates input (defensive programming)
    public Space(String id, String name, int capacity) {
        this.id = Objects.requireNonNull(id, "Space ID cannot be null");
        this.name = Objects.requireNonNull(name, "Space name cannot be null");

        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be positive");
        }

        this.capacity = capacity;
        this.status = RoomStatus.ACTIVE;
        this.blockedTimeSlots = new ArrayList<>();
    }

    // ========================================================================
    // OOP CONCEPT: ABSTRACT METHODS (Polymorphism)
    // Each subclass MUST provide its own implementation
    // ========================================================================

    /** Returns the type name of this space (e.g., "LectureHall", "ComputerLab") */
    public abstract String getSpaceType();

    /** Checks if this space type can host the given activity — POLYMORPHISM */
    public abstract boolean canAccommodateActivityType(ActivityType activityType);

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
        return status == RoomStatus.ACTIVE;
    }

    @Override
    public String getStatusDescription() {
        return String.format("%s [%s] - Capacity: %d, Booked: %d/40 slots",
                name, status.getDisplayName(), capacity, blockedTimeSlots.size());
    }

    // ========================================================================
    // INTERFACE IMPLEMENTATION: Comparable (for sorting rooms by utilization)
    // ========================================================================

    @Override
    public int compareTo(Space other) {
        return Double.compare(this.getUtilizationPercentage(), other.getUtilizationPercentage());
    }

    // ENCAPSULATION: Getters provide read-only access
    public int getCapacity() {
        return capacity;
    }

    public RoomStatus getStatus() {
        return status;
    }

    public void setStatus(RoomStatus status) {
        this.status = Objects.requireNonNull(status, "Status cannot be null");
    }

    public List<TimeSlot> getBlockedTimeSlots() {
        // ENCAPSULATION: Return unmodifiable view
        return Collections.unmodifiableList(blockedTimeSlots);
    }

    /**
     * Check if space is free during a specific time slot
     * Hard constraint: No overlapping bookings
     */
    public boolean isFreeAt(TimeSlot slot) {
        if (!isAvailable()) {
            return false;
        }
        return blockedTimeSlots.stream()
                .noneMatch(blocked -> blocked.overlaps(slot));
    }

    /**
     * Book this space for a time slot
     */
    public void bookTimeSlot(TimeSlot slot) {
        if (!isFreeAt(slot)) {
            throw new IllegalStateException("Time slot is not available for booking");
        }
        blockedTimeSlots.add(slot);
    }

    /**
     * Release a time slot booking
     */
    public void releaseTimeSlot(TimeSlot slot) {
        blockedTimeSlots.remove(slot);
    }

    /**
     * Clear all bookings (used when regenerating timetable)
     */
    public void clearAllBookings() {
        blockedTimeSlots.clear();
    }

    /**
     * Check utilization percentage
     */
    public double getUtilizationPercentage() {
        // 40 slots per week (5 days * 8 hours)
        int totalSlots = 40;
        return (double) blockedTimeSlots.size() / totalSlots * 100;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Space space = (Space) o;
        return id.equals(space.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("%s{id='%s', name='%s', capacity=%d, status=%s}",
                getSpaceType(), id, name, capacity, status);
    }
}
