package com.uniplanner.algorithm;

import com.uniplanner.models.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * ========================================================================
 * OOP CONCEPTS: COMPOSITION + GENERICS + STREAMS + ENCAPSULATION
 * ========================================================================
 * Constraint Satisfaction Problem (CSP) Solver for timetable generation.
 *
 * - COMPOSITION: Contains lists of model objects (Section, Faculty, Space, etc.)
 * - GENERICS: Uses List<Section>, Map<String, Subject>, etc.
 * - STREAMS API: Functional-style filtering and sorting of collections
 * - ENCAPSULATION: Algorithm logic is private; only generateTimetable() is public
 *
 * Hard Constraints (MUST be satisfied):
 * 1. No faculty double-booking
 * 2. No room overlaps
 * 3. Room type matches activity (LectureHall for lectures, Labs for practicals)
 * 4. Section cannot attend two activities at the same time
 *
 * Soft Constraints (optimized):
 * 1. Balance faculty workload (prefer GREEN status)
 * 2. Balance room utilization
 * ========================================================================
 */
public class SchedulingAlgorithm {

    // ENCAPSULATION: Private fields — algorithm internals hidden from callers
    private final List<Section> sections;
    private final List<Faculty> faculties;
    private final List<Space> spaces;
    private final List<TimeSlot> availableTimeSlots;
    private final Map<String, Faculty> facultyMap;      // GENERICS
    private final Map<String, Subject> subjectMap;      // GENERICS
    private final Timetable generatedTimetable;
    private int slotCounter = 0;

    public SchedulingAlgorithm(List<Section> sections, List<Faculty> faculties,
                               List<Space> spaces, List<TimeSlot> availableTimeSlots,
                               List<Subject> subjects) {
        this.sections = Objects.requireNonNull(sections, "Sections cannot be null");
        this.faculties = Objects.requireNonNull(faculties, "Faculties cannot be null");
        this.spaces = Objects.requireNonNull(spaces, "Spaces cannot be null");
        this.availableTimeSlots = Objects.requireNonNull(availableTimeSlots, "TimeSlots cannot be null");

        // Build lookup maps for O(1) access — GENERICS + COLLECTIONS
        this.facultyMap = new HashMap<>();
        for (Faculty f : faculties) {
            facultyMap.put(f.getId(), f);
        }

        this.subjectMap = new HashMap<>();
        for (Subject s : subjects) {
            subjectMap.put(s.getId(), s);
        }

        this.generatedTimetable = new Timetable("TT_" + UUID.randomUUID().toString(),
                "Multi-Department", "2024-2025");
    }

    /**
     * MAIN ALGORITHM: Generate timetable using greedy assignment with constraint satisfaction.
     * Public API — the only method callers need to use.
     */
    public Timetable generateTimetable() {
        generatedTimetable.reset();
        slotCounter = 0;

        System.out.println("\n=== Starting Timetable Generation ===");
        System.out.println("Sections: " + sections.size());
        System.out.println("Faculty: " + faculties.size());
        System.out.println("Rooms: " + spaces.size());
        System.out.println("Time Slots: " + availableTimeSlots.size());

        // Reset all faculty workloads
        for (Faculty f : faculties) {
            f.resetWeeklyAssignment();
        }

        // Reset all room bookings
        for (Space space : spaces) {
            space.clearAllBookings();
        }

        // Process each section and its subjects
        for (Section section : sections) {
            System.out.println("\nScheduling section: " + section.getSectionName());
            List<String> subjectIds = new ArrayList<>(section.getAssignedSubjects());

            for (String subjectId : subjectIds) {
                Subject subject = subjectMap.get(subjectId);
                if (subject == null) {
                    System.out.println("  WARNING: Subject " + subjectId + " not found in subject map");
                    continue;
                }

                System.out.println("  Subject: " + subject.getName() +
                        " (L:" + subject.getLectureHours() +
                        " T:" + subject.getTutorialHours() +
                        " P:" + subject.getPracticalHours() + ")");

                // Schedule Lecture hours
                if (subject.getLectureHours() > 0) {
                    scheduleActivity(section, subject, subject.getLectureHours(),
                            Space.ActivityType.LECTURE);
                }

                // Schedule Tutorial hours
                if (subject.getTutorialHours() > 0) {
                    scheduleActivity(section, subject, subject.getTutorialHours(),
                            Space.ActivityType.TUTORIAL);
                }

                // Schedule Practical hours (requires lab)
                if (subject.getPracticalHours() > 0) {
                    scheduleActivity(section, subject, subject.getPracticalHours(),
                            Space.ActivityType.PRACTICAL);
                }
            }
        }

        generatedTimetable.markAsGenerated();

        System.out.println("\n=== Timetable Generation Complete ===");
        System.out.println("Total slots: " + generatedTimetable.getScheduledSlots().size());
        System.out.println("Conflicts: " + generatedTimetable.getConflictingSlots().size());
        System.out.println("Valid: " + generatedTimetable.isValid());

        return generatedTimetable;
    }

    /**
     * Schedule a specific activity type for a section's subject.
     * ENCAPSULATION: Private helper — internal algorithm logic.
     */
    private void scheduleActivity(Section section, Subject subject, int hours,
                                  Space.ActivityType activityType) {
        for (int i = 0; i < hours; i++) {
            // Step 1: Find best faculty (STREAMS API + soft constraint optimization)
            Faculty faculty = findBestFaculty(subject);
            if (faculty == null) {
                System.out.println("    CONFLICT: No faculty for " + subject.getName() + " " + activityType);
                addConflictSlot(section, subject, null, activityType, "No available faculty with expertise");
                continue;
            }

            // Step 2: Find available room matching activity type (POLYMORPHISM)
            Space room = findAvailableRoom(section, activityType);
            if (room == null) {
                System.out.println("    CONFLICT: No room for " + subject.getName() + " " + activityType);
                addConflictSlot(section, subject, faculty, activityType, "No available room of correct type");
                continue;
            }

            // Step 3: Find time slot free for section, faculty, and room
            TimeSlot slot = findAvailableTimeSlot(section, faculty, room);
            if (slot == null) {
                System.out.println("    CONFLICT: No time slot for " + subject.getName() + " " + activityType);
                addConflictSlot(section, subject, faculty, activityType, "No available time slot");
                continue;
            }

            // Step 4: Create schedule slot and book resources
            String slotId = "SLOT_" + (++slotCounter);
            ScheduleSlot scheduleSlot = new ScheduleSlot(slotId, section, subject, faculty, room, slot, activityType);

            // Book resources (mutate state)
            room.bookTimeSlot(slot);
            faculty.assignHours(1);

            generatedTimetable.addScheduleSlot(scheduleSlot);
            System.out.println("    OK: " + scheduleSlot.getScheduleDescription());
        }
    }

    /**
     * Find the best faculty for a subject using STREAMS API.
     * Hard constraint: Must have expertise in the subject.
     * Soft constraint: Prefer faculty with lower workload (GREEN > AMBER > RED).
     */
    private Faculty findBestFaculty(Subject subject) {
        // STREAMS + GENERICS + LAMBDA: Functional-style collection processing
        List<Faculty> qualified = faculties.stream()
                .filter(f -> f.hasExpertise(subject.getName()))       // Hard: has expertise
                .filter(f -> f.canTakeHours(1))                       // Hard: has capacity
                .sorted()                                              // COMPARABLE: sort by workload
                .collect(Collectors.toList());

        if (qualified.isEmpty()) {
            // Fallback: try matching by department
            qualified = faculties.stream()
                    .filter(f -> f.getDepartment().equalsIgnoreCase(subject.getDepartment()))
                    .filter(f -> f.canTakeHours(1))
                    .sorted()
                    .collect(Collectors.toList());
        }

        return qualified.isEmpty() ? null : qualified.get(0);
    }

    /**
     * Find an available room that supports the activity type.
     * Uses POLYMORPHISM — canAccommodateActivityType() behaves differently
     * for LectureHall vs ComputerLab.
     */
    private Space findAvailableRoom(Section section, Space.ActivityType activityType) {
        int requiredCapacity = section.getStudentStrength();

        // STREAMS + POLYMORPHISM: space.canAccommodateActivityType() dispatches to correct subclass
        return spaces.stream()
                .filter(Space::isAvailable)                                    // Hard: active status
                .filter(space -> space.canAccommodateActivityType(activityType)) // Hard: room type match (POLYMORPHISM)
                .filter(space -> space.getCapacity() >= requiredCapacity)       // Hard: capacity
                .min(Comparator.comparingDouble(Space::getUtilizationPercentage)) // Soft: balance usage
                .orElse(null);
    }

    /**
     * Find a time slot where faculty, section, and room are all free.
     * Hard constraint: No double-booking of any resource.
     */
    private TimeSlot findAvailableTimeSlot(Section section, Faculty faculty, Space room) {
        for (TimeSlot slot : availableTimeSlots) {
            if (isFacultyFreeAt(faculty, slot) && isSectionFreeAt(section, slot) && room.isFreeAt(slot)) {
                return slot;
            }
        }
        return null;
    }

    /**
     * Hard constraint: No faculty double-booking.
     */
    private boolean isFacultyFreeAt(Faculty faculty, TimeSlot slot) {
        return generatedTimetable.getSlotsByFaculty(faculty.getId()).stream()
                .noneMatch(s -> s.getTimeSlot().overlaps(slot));
    }

    /**
     * Hard constraint: A section cannot attend two activities at the same time.
     */
    private boolean isSectionFreeAt(Section section, TimeSlot slot) {
        return generatedTimetable.getSlotsBySection(section.getId()).stream()
                .noneMatch(s -> s.getTimeSlot().overlaps(slot));
    }

    /**
     * Add a conflict slot using the STATIC FACTORY METHOD.
     * This avoids the IllegalArgumentException that the normal constructor
     * would throw when room type doesn't match activity type.
     */
    private void addConflictSlot(Section section, Subject subject, Faculty faculty,
                                 Space.ActivityType activityType, String reason) {
        String slotId = "CONFLICT_" + (++slotCounter);

        if (faculty == null) {
            // Can't create a slot without faculty — just log it
            System.out.println("    [Conflict logged] " + reason + " for " + section.getSectionName());
            return;
        }

        // Use any available room and time slot as placeholders
        Space dummyRoom = spaces.isEmpty() ? null : spaces.get(0);
        TimeSlot dummySlot = availableTimeSlots.isEmpty() ?
                new TimeSlot(TimeSlot.DayOfWeek.MONDAY, java.time.LocalTime.of(9, 0),
                        java.time.LocalTime.of(10, 0)) : availableTimeSlots.get(0);

        if (dummyRoom != null) {
            // OOP: STATIC FACTORY METHOD — bypasses room-type validation for conflict tracking
            ScheduleSlot conflictSlot = ScheduleSlot.createConflictSlot(
                    slotId, section, subject, faculty, dummyRoom, dummySlot, activityType, reason);
            generatedTimetable.addScheduleSlot(conflictSlot);
        }
    }

    public Timetable getTimetable() {
        return generatedTimetable;
    }

    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalSections", sections.size());
        stats.put("totalFaculty", faculties.size());
        stats.put("totalRooms", spaces.size());
        stats.put("generatedSlots", generatedTimetable.getScheduledSlots().size());
        stats.put("conflictingSlots", generatedTimetable.getConflictingSlots().size());
        return stats;
    }
}
