package com.uniplanner.models;

import java.util.*;

/**
 * Represents a section of students for a specific branch/department
 * Example: CS-A, CS-B, EC-A
 */
public class Section {
    private final String id;
    private final String sectionName;
    private final String branch;
    private final int studentStrength;
    private final Set<String> assignedSubjects;
    private final int semesterYear;

    public Section(String id, String sectionName, String branch, int studentStrength, int semesterYear) {
        this.id = Objects.requireNonNull(id, "Section ID cannot be null");
        this.sectionName = Objects.requireNonNull(sectionName, "Section name cannot be null");
        this.branch = Objects.requireNonNull(branch, "Branch cannot be null");

        if (studentStrength <= 0 || studentStrength > 500) {
            throw new IllegalArgumentException("Student strength must be between 1 and 500");
        }

        this.studentStrength = studentStrength;
        this.semesterYear = semesterYear;
        this.assignedSubjects = new HashSet<>();
    }

    // Getters (encapsulation)
    public String getId() {
        return id;
    }

    public String getSectionName() {
        return sectionName;
    }

    public String getBranch() {
        return branch;
    }

    public int getStudentStrength() {
        return studentStrength;
    }

    public int getSemesterYear() {
        return semesterYear;
    }

    public Set<String> getAssignedSubjects() {
        return Collections.unmodifiableSet(assignedSubjects);
    }

    /**
     * Add a subject to this section's curriculum
     */
    public void addSubject(String subjectId) {
        assignedSubjects.add(Objects.requireNonNull(subjectId, "Subject ID cannot be null"));
    }

    /**
     * Remove a subject from this section
     */
    public void removeSubject(String subjectId) {
        assignedSubjects.remove(subjectId);
    }

    /**
     * Check if section has a particular subject
     */
    public boolean hasSubject(String subjectId) {
        return assignedSubjects.contains(subjectId);
    }

    /**
     * Get number of assigned subjects
     */
    public int getAssignedSubjectCount() {
        return assignedSubjects.size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Section section = (Section) o;
        return id.equals(section.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("Section{id='%s', name='%s', branch='%s', students=%d, subjects=%d}",
                id, sectionName, branch, studentStrength, assignedSubjects.size());
    }
}
