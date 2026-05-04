package com.uniplanner.models;

/**
 * ========================================================================
 * OOP CONCEPT: INTERFACE (Abstraction)
 * ========================================================================
 * Defines a contract for any entity that can be scheduled in the timetable.
 * Both Faculty and Space implement this interface, demonstrating
 * interface-based polymorphism — they can be treated uniformly
 * through this common type.
 *
 * Java OOP Principle: Programming to an interface, not an implementation.
 * ========================================================================
 */
public interface Schedulable {

    /**
     * Get the unique identifier of this schedulable entity
     */
    String getId();

    /**
     * Get the display name
     */
    String getName();

    /**
     * Check if this entity is currently available for scheduling
     */
    boolean isAvailable();

    /**
     * Get a human-readable description of the entity's current status
     */
    String getStatusDescription();
}
