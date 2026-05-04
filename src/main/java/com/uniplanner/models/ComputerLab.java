package com.uniplanner.models;

/**
 * ========================================================================
 * OOP CONCEPT: INHERITANCE + POLYMORPHISM
 * ========================================================================
 * Concrete subclass of Space for Computer Labs.
 *
 * - INHERITANCE: Extends abstract Space class
 * - POLYMORPHISM: Overrides canAccommodateActivityType() — labs accept
 *   PRACTICAL and TUTORIAL but NOT pure lectures
 * - ENCAPSULATION: Lab-specific fields (systems, OS) are private
 * ========================================================================
 */
public class ComputerLab extends Space {

    // ENCAPSULATION: Private fields unique to ComputerLab
    private final int numberOfSystems;
    private final String operatingSystem;
    private final String softwareAvailable;
    private final boolean hasProjector;

    public ComputerLab(String id, String name, int capacity, int numberOfSystems,
                       String operatingSystem) {
        super(id, name, capacity);
        this.numberOfSystems = numberOfSystems;
        this.operatingSystem = operatingSystem;
        this.softwareAvailable = "";
        this.hasProjector = true;
    }

    /**
     * OOP CONCEPT: CONSTRUCTOR OVERLOADING
     */
    public ComputerLab(String id, String name, int capacity, int numberOfSystems,
                       String operatingSystem, String softwareAvailable, boolean hasProjector) {
        super(id, name, capacity);
        this.numberOfSystems = numberOfSystems;
        this.operatingSystem = operatingSystem;
        this.softwareAvailable = softwareAvailable;
        this.hasProjector = hasProjector;
    }

    /**
     * OOP CONCEPT: POLYMORPHISM — ComputerLab returns "ComputerLab"
     * while LectureHall returns "LectureHall"
     */
    @Override
    public String getSpaceType() {
        return "ComputerLab";
    }

    /**
     * OOP CONCEPT: POLYMORPHISM — ComputerLab-specific scheduling rules
     * Labs accommodate PRACTICAL and TUTORIAL but NOT pure lectures
     * (contrasts with LectureHall which does LECTURE + TUTORIAL)
     */
    @Override
    public boolean canAccommodateActivityType(ActivityType activityType) {
        return activityType == ActivityType.PRACTICAL || activityType == ActivityType.TUTORIAL;
    }

    // ENCAPSULATION: Getters
    public int getNumberOfSystems() {
        return numberOfSystems;
    }

    public String getOperatingSystem() {
        return operatingSystem;
    }

    public String getSoftwareAvailable() {
        return softwareAvailable;
    }

    public boolean hasProjector() {
        return hasProjector;
    }

    /**
     * Check if lab has enough systems for student strength
     */
    public boolean hasSufficientSystems(int studentCount) {
        return numberOfSystems >= studentCount;
    }

    @Override
    public String toString() {
        return String.format("ComputerLab{id='%s', name='%s', capacity=%d, systems=%d, os=%s}",
                id, name, capacity, numberOfSystems, operatingSystem);
    }
}
