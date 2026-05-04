package com.uniplanner.models;

/**
 * ========================================================================
 * OOP CONCEPT: INHERITANCE + POLYMORPHISM
 * ========================================================================
 * Concrete subclass of Space for Lecture Halls.
 *
 * - INHERITANCE: Extends abstract Space class, inheriting all common behavior
 * - POLYMORPHISM: Overrides getSpaceType() and canAccommodateActivityType()
 *   to provide lecture-hall-specific behavior
 * - CONSTRUCTOR CHAINING: Calls super() to initialize parent fields
 * - METHOD OVERRIDING: @Override annotation ensures correct signature
 * ========================================================================
 */
public class LectureHall extends Space {

    // ENCAPSULATION: Private fields specific to LectureHall
    private final boolean hasProjector;
    private final boolean hasWhiteboard;
    private final boolean hasAirConditioning;

    /**
     * OOP CONCEPT: CONSTRUCTOR CHAINING — calls super(id, name, capacity)
     */
    public LectureHall(String id, String name, int capacity) {
        super(id, name, capacity);  // INHERITANCE: delegate to parent constructor
        this.hasProjector = true;
        this.hasWhiteboard = true;
        this.hasAirConditioning = false;
    }

    /**
     * OOP CONCEPT: CONSTRUCTOR OVERLOADING — same class, different parameters
     */
    public LectureHall(String id, String name, int capacity, boolean hasProjector,
                       boolean hasWhiteboard, boolean hasAirConditioning) {
        super(id, name, capacity);
        this.hasProjector = hasProjector;
        this.hasWhiteboard = hasWhiteboard;
        this.hasAirConditioning = hasAirConditioning;
    }

    /**
     * OOP CONCEPT: POLYMORPHISM — each subclass returns its own type name
     */
    @Override
    public String getSpaceType() {
        return "LectureHall";
    }

    /**
     * OOP CONCEPT: POLYMORPHISM — LectureHall-specific scheduling rules
     * Lecture halls can accommodate LECTURES and TUTORIALS but NOT practicals
     */
    @Override
    public boolean canAccommodateActivityType(ActivityType activityType) {
        return activityType == ActivityType.LECTURE || activityType == ActivityType.TUTORIAL;
    }

    // ENCAPSULATION: Getters for private fields
    public boolean hasProjector() {
        return hasProjector;
    }

    public boolean hasWhiteboard() {
        return hasWhiteboard;
    }

    public boolean hasAirConditioning() {
        return hasAirConditioning;
    }

    @Override
    public String toString() {
        return String.format("LectureHall{id='%s', name='%s', capacity=%d, status=%s, AC=%s}",
                id, name, capacity, status, hasAirConditioning);
    }
}
