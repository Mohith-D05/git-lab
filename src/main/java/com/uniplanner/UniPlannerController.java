package com.uniplanner;

import com.uniplanner.models.*;
import com.uniplanner.algorithm.SchedulingAlgorithm;
import com.uniplanner.persistence.DatabaseManager;

import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

import java.time.LocalTime;
import java.util.*;

/**
 * ========================================================================
 * OOP CONCEPT: MVC PATTERN (Controller) + SERVICE LAYER
 * ========================================================================
 * Main Application Controller — orchestrates the timetable generation system.
 * Demonstrates MVC pattern: Model (entities) + Controller (this) + View (HTML/API)
 *
 * - Spring @Service: Managed as a singleton bean by Spring IoC container
 * - COMPOSITION: Contains lists of all model objects
 * - ENCAPSULATION: Private fields, public methods for controlled access
 * - COLLECTIONS: Uses ArrayList, LinkedHashMap, Optional, streams
 * ========================================================================
 */
@Service
public class UniPlannerController {

    // ENCAPSULATION: Private mutable state
    private final DatabaseManager dbManager;
    private List<Faculty> faculties;
    private List<Subject> subjects;
    private List<Section> sections;
    private List<Space> spaces;
    private List<TimeSlot> timeSlots;
    private Timetable currentTimetable;

    public UniPlannerController(DatabaseManager dbManager) {
        this.dbManager = dbManager;
        this.faculties = new ArrayList<>();
        this.subjects = new ArrayList<>();
        this.sections = new ArrayList<>();
        this.spaces = new ArrayList<>();
        this.timeSlots = new ArrayList<>();
    }

    /**
     * Initialize on Spring startup — load from DB and populate defaults
     */
    @PostConstruct
    public void initialize() {
        loadDataFromDatabase();
        initializeDefaultTimeSlots();
        loadSampleData();
        System.out.println("[Controller] Initialized with " + faculties.size() + " faculty, "
                + subjects.size() + " subjects, " + sections.size() + " sections, "
                + spaces.size() + " rooms, " + timeSlots.size() + " time slots");
    }

    private void loadDataFromDatabase() {
        this.faculties = dbManager.getAllFaculties();
        this.subjects = dbManager.getAllSubjects();
        this.sections = dbManager.getAllSections();
        this.spaces = dbManager.getAllSpaces();
        this.timeSlots = dbManager.getAllTimeSlots();
    }

    /**
     * Initialize standard time slots (9 AM - 5 PM, 1-hour slots, Monday-Friday)
     */
    private void initializeDefaultTimeSlots() {
        if (!timeSlots.isEmpty()) {
            return;
        }

        TimeSlot.DayOfWeek[] days = {
                TimeSlot.DayOfWeek.MONDAY,
                TimeSlot.DayOfWeek.TUESDAY,
                TimeSlot.DayOfWeek.WEDNESDAY,
                TimeSlot.DayOfWeek.THURSDAY,
                TimeSlot.DayOfWeek.FRIDAY
        };

        for (TimeSlot.DayOfWeek day : days) {
            for (int hour = 9; hour < 17; hour++) {
                TimeSlot slot = new TimeSlot(day,
                        LocalTime.of(hour, 0),
                        LocalTime.of(hour + 1, 0));
                timeSlots.add(slot);
                dbManager.saveTimeSlot(slot);
            }
        }
    }

    // ============== CRUD OPERATIONS ==============

    public void addFaculty(Faculty faculty) {
        faculties.removeIf(f -> f.getId().equals(faculty.getId()));
        faculties.add(faculty);
        dbManager.saveFaculty(faculty);
    }

    public void addSubject(Subject subject) {
        subjects.removeIf(s -> s.getId().equals(subject.getId()));
        subjects.add(subject);
        dbManager.saveSubject(subject);
    }

    public void addSection(Section section) {
        sections.removeIf(s -> s.getId().equals(section.getId()));
        sections.add(section);
        dbManager.saveSection(section);
    }

    public void addSpace(Space space) {
        spaces.removeIf(s -> s.getId().equals(space.getId()));
        spaces.add(space);
        dbManager.saveSpace(space);
    }

    public void deleteFaculty(String facultyId) {
        faculties.removeIf(f -> f.getId().equals(facultyId));
        dbManager.deleteFaculty(facultyId);
    }

    public void deleteSubject(String subjectId) {
        subjects.removeIf(s -> s.getId().equals(subjectId));
        dbManager.deleteSubject(subjectId);
    }

    public void deleteSection(String sectionId) {
        sections.removeIf(s -> s.getId().equals(sectionId));
        dbManager.deleteSection(sectionId);
    }

    /**
     * Toggle room status between Active and Maintenance
     */
    public void toggleRoomStatus(String roomId) {
        spaces.stream()
                .filter(r -> r.getId().equals(roomId))
                .findFirst()
                .ifPresent(r -> {
                    Space.RoomStatus newStatus = r.getStatus() == Space.RoomStatus.ACTIVE ?
                            Space.RoomStatus.MAINTENANCE : Space.RoomStatus.ACTIVE;
                    r.setStatus(newStatus);
                    System.out.println("Room " + r.getName() + " status → " + newStatus);
                });
    }

    // ============== TIMETABLE GENERATION ==============

    /**
     * Generate timetable using the CSP scheduling algorithm.
     * COMPOSITION: Creates SchedulingAlgorithm and delegates.
     */
    public Timetable generateTimetable() {
        System.out.println("\n[Controller] Starting timetable generation...");

        SchedulingAlgorithm algorithm = new SchedulingAlgorithm(
                sections, faculties, spaces, timeSlots, subjects);

        currentTimetable = algorithm.generateTimetable();

        System.out.println("[Controller] " + currentTimetable.getSummary());
        return currentTimetable;
    }

    // ============== QUERY OPERATIONS ==============

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new LinkedHashMap<>();

        long availableFaculty = faculties.stream()
                .filter(f -> f.getWorkloadStatus() == Faculty.WorkloadStatus.GREEN)
                .count();
        stats.put("availableFacultyCount", availableFaculty);
        stats.put("totalFacultyCount", faculties.size());

        double avgUtilization = spaces.stream()
                .mapToDouble(Space::getUtilizationPercentage)
                .average()
                .orElse(0.0);
        stats.put("averageRoomUtilization", String.format("%.2f%%", avgUtilization));

        int conflictCount = currentTimetable != null ? currentTimetable.getTotalConflictCount() : 0;
        stats.put("conflictCount", conflictCount);
        stats.put("timetableGenerated", currentTimetable != null && currentTimetable.isGenerated());
        stats.put("totalSections", sections.size());
        stats.put("totalSubjects", subjects.size());
        stats.put("totalRooms", spaces.size());

        return stats;
    }

    public List<ScheduleSlot> getTimetableForSection(String sectionId) {
        if (currentTimetable == null) return new ArrayList<>();
        return currentTimetable.getSlotsBySection(sectionId);
    }

    public List<ScheduleSlot> getConflictingSlots() {
        if (currentTimetable == null) return new ArrayList<>();
        return currentTimetable.getConflictingSlots();
    }

    public void resolveConflict(String slotId, Faculty newFaculty, Space newRoom) {
        if (currentTimetable == null) return;
        System.out.println("Resolving conflict for slot: " + slotId);
    }

    // ENCAPSULATION: Getters return unmodifiable views
    public List<Faculty> getFaculties() {
        return Collections.unmodifiableList(faculties);
    }

    public List<Subject> getSubjects() {
        return Collections.unmodifiableList(subjects);
    }

    public List<Section> getSections() {
        return Collections.unmodifiableList(sections);
    }

    public List<Space> getSpaces() {
        return Collections.unmodifiableList(spaces);
    }

    public Timetable getCurrentTimetable() {
        return currentTimetable;
    }

    // ============== SAMPLE DATA ==============

    /**
     * Populate system with rich sample data for 3 departments (CS, EC, ME).
     * Uses FACTORY PATTERN for room creation.
     */
    public void loadSampleData() {
        if (!faculties.isEmpty() || !subjects.isEmpty()) {
            return; // Already has data
        }

        System.out.println("[Controller] Loading sample data for CS, EC, ME departments...");

        // ===================== CS DEPARTMENT FACULTY =====================
        Faculty f1 = new Faculty("FAC001", "Dr. Rajesh Kumar", "rajesh@university.edu", "CS", 20);
        f1.addExpertise("Data Structures");
        f1.addExpertise("Algorithms");

        Faculty f2 = new Faculty("FAC002", "Prof. Suresh Sharma", "suresh@university.edu", "CS", 18);
        f2.addExpertise("Database Management");
        f2.addExpertise("SQL");

        Faculty f3 = new Faculty("FAC003", "Dr. Priya Mehta", "priya@university.edu", "CS", 20);
        f3.addExpertise("Operating Systems");
        f3.addExpertise("Computer Networks");

        Faculty f4 = new Faculty("FAC004", "Prof. Amit Singh", "amit@university.edu", "CS", 16);
        f4.addExpertise("Object Oriented Programming");
        f4.addExpertise("Java Programming");

        // ===================== EC DEPARTMENT FACULTY =====================
        Faculty f5 = new Faculty("FAC005", "Dr. Neha Verma", "neha@university.edu", "EC", 20);
        f5.addExpertise("Digital Electronics");
        f5.addExpertise("VLSI Design");

        Faculty f6 = new Faculty("FAC006", "Prof. Vikram Joshi", "vikram@university.edu", "EC", 18);
        f6.addExpertise("Signal Processing");
        f6.addExpertise("Communication Systems");

        // ===================== ME DEPARTMENT FACULTY =====================
        Faculty f7 = new Faculty("FAC007", "Dr. Rahul Gupta", "rahul@university.edu", "ME", 20);
        f7.addExpertise("Thermodynamics");
        f7.addExpertise("Heat Transfer");

        Faculty f8 = new Faculty("FAC008", "Prof. Sanjay Patel", "sanjay@university.edu", "ME", 18);
        f8.addExpertise("Engineering Mechanics");
        f8.addExpertise("Machine Design");

        addFaculty(f1); addFaculty(f2); addFaculty(f3); addFaculty(f4);
        addFaculty(f5); addFaculty(f6); addFaculty(f7); addFaculty(f8);

        // ===================== CS SUBJECTS =====================
        Subject s1 = new Subject("SUB001", "Data Structures", "CS", 3, 1, 2, 4);
        Subject s2 = new Subject("SUB002", "Database Management", "CS", 3, 1, 2, 4);
        Subject s3 = new Subject("SUB003", "Operating Systems", "CS", 3, 1, 0, 3);
        Subject s4 = new Subject("SUB004", "Object Oriented Programming", "CS", 3, 0, 2, 4);

        // ===================== EC SUBJECTS =====================
        Subject s5 = new Subject("SUB005", "Digital Electronics", "EC", 3, 1, 2, 4);
        Subject s6 = new Subject("SUB006", "Signal Processing", "EC", 3, 1, 0, 3);

        // ===================== ME SUBJECTS =====================
        Subject s7 = new Subject("SUB007", "Thermodynamics", "ME", 3, 1, 0, 3);
        Subject s8 = new Subject("SUB008", "Engineering Mechanics", "ME", 3, 1, 2, 4);

        addSubject(s1); addSubject(s2); addSubject(s3); addSubject(s4);
        addSubject(s5); addSubject(s6); addSubject(s7); addSubject(s8);

        // ===================== CS SECTIONS =====================
        Section sec1 = new Section("SEC001", "CS-A", "CS", 60, 3);
        sec1.addSubject("SUB001"); sec1.addSubject("SUB002");
        sec1.addSubject("SUB003"); sec1.addSubject("SUB004");

        Section sec2 = new Section("SEC002", "CS-B", "CS", 55, 3);
        sec2.addSubject("SUB001"); sec2.addSubject("SUB002");
        sec2.addSubject("SUB003"); sec2.addSubject("SUB004");

        Section sec3 = new Section("SEC003", "CS-C", "CS", 50, 3);
        sec3.addSubject("SUB001"); sec3.addSubject("SUB002");

        // ===================== EC SECTIONS =====================
        Section sec4 = new Section("SEC004", "EC-A", "EC", 55, 3);
        sec4.addSubject("SUB005"); sec4.addSubject("SUB006");

        Section sec5 = new Section("SEC005", "EC-B", "EC", 50, 3);
        sec5.addSubject("SUB005"); sec5.addSubject("SUB006");

        // ===================== ME SECTIONS =====================
        Section sec6 = new Section("SEC006", "ME-A", "ME", 60, 3);
        sec6.addSubject("SUB007"); sec6.addSubject("SUB008");

        addSection(sec1); addSection(sec2); addSection(sec3);
        addSection(sec4); addSection(sec5); addSection(sec6);

        // ===================== ROOMS (using FACTORY PATTERN) =====================
        // POLYMORPHISM: SpaceFactory returns Space reference but creates specific subclass
        Space lh1 = SpaceFactory.createLectureHall("ROOM001", "LH-101", 100, true, true, true);
        Space lh2 = SpaceFactory.createLectureHall("ROOM002", "LH-102", 80, true, true, false);
        Space lh3 = SpaceFactory.createLectureHall("ROOM003", "LH-201", 120, true, true, true);
        Space lh4 = SpaceFactory.createLectureHall("ROOM004", "LH-202", 70, true, true, false);

        Space lab1 = SpaceFactory.createComputerLab("ROOM005", "CS-Lab-1", 60, 60, "Windows");
        Space lab2 = SpaceFactory.createComputerLab("ROOM006", "CS-Lab-2", 50, 50, "Linux");
        Space lab3 = SpaceFactory.createComputerLab("ROOM007", "EC-Lab-1", 55, 55, "Windows");
        Space lab4 = SpaceFactory.createComputerLab("ROOM008", "ME-Lab-1", 60, 60, "Windows");

        addSpace(lh1); addSpace(lh2); addSpace(lh3); addSpace(lh4);
        addSpace(lab1); addSpace(lab2); addSpace(lab3); addSpace(lab4);

        System.out.println("[Controller] Sample data loaded: " + faculties.size() + " faculty, "
                + subjects.size() + " subjects, " + sections.size() + " sections, "
                + spaces.size() + " rooms");
    }
}
