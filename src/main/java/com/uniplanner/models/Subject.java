package com.uniplanner.models;

import java.util.Objects;

/**
 * Represents a subject offered by a department
 * Encapsulates subject details including lecture, tutorial, and practical hours
 */
public class Subject {
    private final String id;
    private final String name;
    private final String department;
    private final int lectureHours;
    private final int tutorialHours;
    private final int practicalHours;
    private final int creditWeightage;
    private final boolean requiresLab;

    public Subject(String id, String name, String department, int lectureHours,
                   int tutorialHours, int practicalHours, int creditWeightage) {
        this.id = Objects.requireNonNull(id, "Subject ID cannot be null");
        this.name = Objects.requireNonNull(name, "Subject name cannot be null");
        this.department = Objects.requireNonNull(department, "Department cannot be null");

        if (lectureHours < 0 || tutorialHours < 0 || practicalHours < 0 || creditWeightage < 0) {
            throw new IllegalArgumentException("Hours and weightage must be non-negative");
        }

        this.lectureHours = lectureHours;
        this.tutorialHours = tutorialHours;
        this.practicalHours = practicalHours;
        this.creditWeightage = creditWeightage;
        this.requiresLab = practicalHours > 0;
    }

    // Getters (encapsulation)
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDepartment() {
        return department;
    }

    public int getLectureHours() {
        return lectureHours;
    }

    public int getTutorialHours() {
        return tutorialHours;
    }

    public int getPracticalHours() {
        return practicalHours;
    }

    public int getCreditWeightage() {
        return creditWeightage;
    }

    public boolean isRequiresLab() {
        return requiresLab;
    }

    /**
     * Calculates total hours needed for this subject
     */
    public int getTotalHours() {
        return lectureHours + tutorialHours + practicalHours;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Subject subject = (Subject) o;
        return id.equals(subject.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("Subject{%s: %s (L:%d T:%d P:%d)}", id, name, lectureHours, tutorialHours, practicalHours);
    }
}
