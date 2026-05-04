package com.uniplanner.algorithm;

import com.uniplanner.models.ComputerLab;
import com.uniplanner.models.Faculty;
import com.uniplanner.models.LectureHall;
import com.uniplanner.models.Section;
import com.uniplanner.models.Space;
import com.uniplanner.models.Subject;
import com.uniplanner.models.TimeSlot;
import com.uniplanner.models.Timetable;

// JUnit 5 (included with spring-boot-starter-test in Spring Boot 3.x)
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

/**
 * Unit tests for SchedulingAlgorithm.
 * Demonstrates:
 * - Hard constraint satisfaction (faculty expertise matching)
 * - Repeated run independence (no stale state)
 * - Partial expertise matching via hasExpertise()
 */
public class SchedulingAlgorithmTest {

    @Test
    public void generateTimetableShouldWorkOnRepeatedRuns() {
        Faculty faculty = new Faculty("FAC001", "Dr. Rajesh Kumar", "rajesh@example.com", "CS", 20);
        faculty.addExpertise("SUB001");
        faculty.addExpertise("Data Structures");

        Subject subject = new Subject("SUB001", "Data Structures", "CS", 2, 1, 1, 4);

        Section section = new Section("SEC001", "CS-A", "CS", 40, 3);
        section.addSubject(subject.getId());

        List<Space> spaces = Arrays.asList(
                new LectureHall("ROOM001", "LH-101", 80),
                new ComputerLab("ROOM002", "Lab-201", 40, 40, "Windows")
        );

        List<TimeSlot> timeSlots = Arrays.asList(
                new TimeSlot(TimeSlot.DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(10, 0)),
                new TimeSlot(TimeSlot.DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(11, 0)),
                new TimeSlot(TimeSlot.DayOfWeek.MONDAY, LocalTime.of(11, 0), LocalTime.of(12, 0)),
                new TimeSlot(TimeSlot.DayOfWeek.MONDAY, LocalTime.of(12, 0), LocalTime.of(13, 0))
        );

        // First run
        SchedulingAlgorithm firstRun = new SchedulingAlgorithm(
                Arrays.asList(section),
                Arrays.asList(faculty),
                spaces,
                timeSlots,
                Arrays.asList(subject)
        );

        Timetable firstTimetable = firstRun.generateTimetable();
        assertFalse(firstTimetable.getScheduledSlots().isEmpty(), "First run should produce slots");
        assertTrue(firstTimetable.isGenerated(), "Timetable should be marked as generated");

        // Second run — must be independent (no stale bookings)
        SchedulingAlgorithm secondRun = new SchedulingAlgorithm(
                Arrays.asList(section),
                Arrays.asList(faculty),
                spaces,
                timeSlots,
                Arrays.asList(subject)
        );

        Timetable secondTimetable = secondRun.generateTimetable();
        assertFalse(secondTimetable.getScheduledSlots().isEmpty(), "Second run should produce slots");
        assertTrue(secondTimetable.isGenerated(), "Second timetable should be marked as generated");
    }

    @Test
    public void generateTimetableShouldSupportPartialFacultyExpertiseMatches() {
        // Faculty has "Database" expertise — subject is "Database Management"
        // hasExpertise() uses partial matching, so this should succeed
        Faculty faculty = new Faculty("FAC002", "Prof. Suresh Sharma", "suresh@example.com", "CS", 20);
        faculty.addExpertise("Database");

        Subject subject = new Subject("SUB002", "Database Management", "CS", 1, 0, 0, 4);

        Section section = new Section("SEC002", "CS-B", "CS", 30, 3);
        section.addSubject(subject.getId());

        SchedulingAlgorithm algorithm = new SchedulingAlgorithm(
                Arrays.asList(section),
                Arrays.asList(faculty),
                Arrays.asList(new LectureHall("ROOM001", "LH-101", 60)),
                Arrays.asList(new TimeSlot(TimeSlot.DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(10, 0))),
                Arrays.asList(subject)
        );

        Timetable timetable = algorithm.generateTimetable();
        assertTrue(timetable.isGenerated(), "Timetable should be generated");
        assertEquals(1, timetable.getScheduledSlots().size(), "Should schedule exactly 1 lecture slot");
        assertTrue(timetable.getConflictingSlots().isEmpty(), "No conflicts expected");
    }

    @Test
    public void generateTimetableShouldHandleNoAvailableFacultyGracefully() {
        // Faculty has no matching expertise — should not crash but log conflict
        Faculty faculty = new Faculty("FAC003", "Dr. Unknown", "unknown@example.com", "CS", 20);
        faculty.addExpertise("Unrelated Subject");

        Subject subject = new Subject("SUB003", "Quantum Computing", "CS", 1, 0, 0, 3);

        Section section = new Section("SEC003", "CS-C", "CS", 25, 3);
        section.addSubject(subject.getId());

        SchedulingAlgorithm algorithm = new SchedulingAlgorithm(
                Arrays.asList(section),
                Arrays.asList(faculty),
                Arrays.asList(new LectureHall("ROOM001", "LH-101", 50)),
                Arrays.asList(new TimeSlot(TimeSlot.DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(10, 0))),
                Arrays.asList(subject)
        );

        // Should not throw — handles gracefully
        Timetable timetable = algorithm.generateTimetable();
        assertTrue(timetable.isGenerated(), "Timetable should still be marked generated even with conflicts");
    }
}
