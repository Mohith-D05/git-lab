package com.uniplanner.models;

/**
 * ========================================================================
 * OOP CONCEPT: FACTORY PATTERN (Creational Design Pattern)
 * ========================================================================
 * Encapsulates the object creation logic for Space subclasses.
 * The client code doesn't need to know the concrete class names —
 * it just passes a type string and gets the correct object back.
 *
 * This demonstrates:
 * - Factory Method Pattern
 * - Polymorphism (returns Space reference to LectureHall or ComputerLab)
 * - Encapsulation (hides creation details)
 * ========================================================================
 */
public class SpaceFactory {

    /**
     * Create a Space (room) based on the given type string.
     * Returns the appropriate subclass instance.
     *
     * @param type "LectureHall" or "ComputerLab"
     * @param id   unique identifier
     * @param name display name
     * @param capacity seating capacity
     * @return Space subclass instance
     * @throws IllegalArgumentException if type is unknown
     */
    public static Space createSpace(String type, String id, String name, int capacity) {
        // POLYMORPHISM: Return type is abstract Space, but actual object is a subclass
        switch (type) {
            case "LectureHall":
                return new LectureHall(id, name, capacity);
            case "ComputerLab":
                return new ComputerLab(id, name, capacity, capacity, "Windows");
            default:
                throw new IllegalArgumentException("Unknown space type: " + type
                        + ". Supported types: LectureHall, ComputerLab");
        }
    }

    /**
     * Create a ComputerLab with full configuration
     */
    public static ComputerLab createComputerLab(String id, String name, int capacity,
                                                  int systems, String os) {
        return new ComputerLab(id, name, capacity, systems, os);
    }

    /**
     * Create a LectureHall with full configuration
     */
    public static LectureHall createLectureHall(String id, String name, int capacity,
                                                  boolean projector, boolean whiteboard, boolean ac) {
        return new LectureHall(id, name, capacity, projector, whiteboard, ac);
    }
}
