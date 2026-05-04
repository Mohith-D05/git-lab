# UniPlanner Implementation Guide

> Complete step-by-step guide to building and extending the system

## TABLE OF CONTENTS
1. [Phase 1: Data Modeling](#phase-1-data-modeling)
2. [Phase 2: Scheduling Algorithm](#phase-2-scheduling-algorithm)
3. [Phase 3: Database & API](#phase-3-database--api)
4. [Phase 4: Frontend Integration](#phase-4-frontend-integration)
5. [Testing & Debugging](#testing--debugging)
6. [Deployment](#deployment)

---

## PHASE 1: DATA MODELING

### Objective
Create robust entity classes with OOP principles to represent all system entities.

### Step 1.1: TimeSlot (Immutable Value Object)

```java
// File: src/main/java/com/uniplanner/models/TimeSlot.java

public class TimeSlot {
    private final DayOfWeek day;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final int durationMinutes;

    public enum DayOfWeek {
        MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY
    }

    public TimeSlot(DayOfWeek day, LocalTime startTime, LocalTime endTime) {
        // Constructor logic with validation
    }

    // Key method: Check overlap between time slots
    public boolean overlaps(TimeSlot other) {
        if (!this.day.equals(other.day)) {
            return false;  // Different days = no overlap
        }
        // Same day: check if times overlap
        return this.startTime.isBefore(other.endTime) && 
               other.startTime.isBefore(this.endTime);
    }

    // Example usage
    public static void main(String[] args) {
        TimeSlot slot1 = new TimeSlot(
            DayOfWeek.MONDAY, 
            LocalTime.of(9, 0), 
            LocalTime.of(10, 0)
        );
        
        TimeSlot slot2 = new TimeSlot(
            DayOfWeek.MONDAY, 
            LocalTime.of(9, 30), 
            LocalTime.of(10, 30)
        );
        
        System.out.println(slot1.overlaps(slot2));  // true - they overlap!
    }
}
```

**Exercises:**
1. Add method: `public long getOverlapDuration(TimeSlot other)`
2. Add method: `public boolean isConsecutiveTo(TimeSlot other)`

---

### Step 1.2: Subject (Domain Entity)

```java
public class Subject {
    private final String id;
    private final String name;
    private final String department;
    private final int lectureHours;
    private final int tutorialHours;
    private final int practicalHours;
    private final int creditWeightage;

    // Constructor with validation
    public Subject(String id, String name, String department, 
                   int lectureHours, int tutorialHours, 
                   int practicalHours, int creditWeightage) {
        // Validate all inputs
        this.id = Objects.requireNonNull(id);
        // ... more initialization
    }

    // Derived property
    public int getTotalHours() {
        return lectureHours + tutorialHours + practicalHours;
    }

    public boolean isRequiresLab() {
        return practicalHours > 0;
    }

    // Example: Real-world usage
    public static void main(String[] args) {
        Subject ds = new Subject(
            "SUB001",
            "Data Structures",
            "CS",
            3, 1, 2,  // L/T/P hours
            4         // credits
        );

        System.out.println("Total hours: " + ds.getTotalHours());  // 6
        System.out.println("Requires Lab: " + ds.isRequiresLab()); // true
        System.out.println("Credits: " + ds.getCreditWeightage()); // 4
    }
}
```

**Exercise:** Create a Subject for "Basic Programming" (3L/1T/1P, 3 credits)

---

### Step 1.3: Space Hierarchy (Abstraction + Inheritance)

```java
// Abstract Base Class
public abstract class Space {
    protected final String id;
    protected final String name;
    protected final int capacity;
    protected RoomStatus status;
    protected final List<TimeSlot> blockedTimeSlots;

    // Template methods (concrete behavior)
    public void bookTimeSlot(TimeSlot slot) {
        if (!isFreeAt(slot)) {
            throw new IllegalStateException("Slot not available");
        }
        blockedTimeSlots.add(slot);
    }

    public boolean isFreeAt(TimeSlot slot) {
        return blockedTimeSlots.stream()
                .noneMatch(blocked -> blocked.overlaps(slot));
    }

    // Abstract methods (must override)
    public abstract String getSpaceType();
    public abstract boolean canAccommodateActivityType(ActivityType type);
}

// Concrete Implementation #1
public class LectureHall extends Space {
    private final boolean hasProjector;

    @Override
    public String getSpaceType() {
        return "LectureHall";
    }

    @Override
    public boolean canAccommodateActivityType(ActivityType type) {
        return type == ActivityType.LECTURE || type == ActivityType.TUTORIAL;
    }
}

// Concrete Implementation #2
public class ComputerLab extends Space {
    private final int numberOfSystems;

    @Override
    public String getSpaceType() {
        return "ComputerLab";
    }

    @Override
    public boolean canAccommodateActivityType(ActivityType type) {
        return type == ActivityType.PRACTICAL;
    }

    public boolean hasSufficientSystems(int studentCount) {
        return numberOfSystems >= studentCount;
    }
}

// Polymorphic usage
public static void main(String[] args) {
    List<Space> spaces = Arrays.asList(
        new LectureHall("LH101", "Lecture Hall 101", 100),
        new ComputerLab("LAB201", "Computer Lab 201", 50)
    );

    // Same code, different behavior!
    for (Space space : spaces) {
        System.out.println(space.getSpaceType());
        System.out.println("Can do LECTURE? " + 
            space.canAccommodateActivityType(ActivityType.LECTURE));
        System.out.println("Can do PRACTICAL? " + 
            space.canAccommodateActivityType(ActivityType.PRACTICAL));
    }

    // Output:
    // LectureHall
    // Can do LECTURE? true
    // Can do PRACTICAL? false
    // 
    // ComputerLab
    // Can do LECTURE? false
    // Can do PRACTICAL? true
}
```

**Challenge:** Create a new room type `Auditorium` that can accommodate all activity types

---

### Step 1.4: Faculty (Encapsulation Example)

```java
public class Faculty {
    private final String id;
    private int assignedHoursThisWeek;  // Mutable, private
    private WorkloadStatus workloadStatus;  // Derived state

    public Faculty(String id, String name, String email, 
                   String department, int maxHoursPerWeek) {
        // Validation
        if (maxHoursPerWeek <= 0 || maxHoursPerWeek > 50) {
            throw new IllegalArgumentException("Invalid max hours");
        }
    }

    // Controlled assignment - with validation!
    public void assignHours(int hours) {
        if (!canTakeHours(hours)) {
            throw new IllegalArgumentException(
                "Would exceed " + maxHoursPerWeek + " hour limit"
            );
        }
        this.assignedHoursThisWeek += hours;
        updateWorkloadStatus();  // Keep state consistent
    }

    public boolean canTakeHours(int hours) {
        return (assignedHoursThisWeek + hours) <= maxHoursPerWeek;
    }

    // Private helper: nobody can force invalid state
    private void updateWorkloadStatus() {
        double percentage = getWorkloadPercentage();
        if (percentage <= 50) {
            workloadStatus = WorkloadStatus.GREEN;
        } else if (percentage <= 80) {
            workloadStatus = WorkloadStatus.AMBER;
        } else {
            workloadStatus = WorkloadStatus.RED;
        }
    }

    public double getWorkloadPercentage() {
        return (double) assignedHoursThisWeek / maxHoursPerWeek * 100;
    }

    // Demo
    public static void main(String[] args) {
        Faculty faculty = new Faculty(
            "FAC001", "Dr. Kumar", "kumar@uni.edu", "CS", 20
        );

        faculty.assignHours(8);  // Now at 8/20 (40%) = GREEN
        System.out.println(faculty.getWorkloadStatus()); // GREEN

        faculty.assignHours(10); // Now at 18/20 (90%) = RED
        System.out.println(faculty.getWorkloadStatus()); // RED

        try {
            faculty.assignHours(5);  // Would be 23/20 - INVALID!
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
```

**Benefit:** `assignedHoursThisWeek` can NEVER be in an invalid state!

---

### Step 1.5: Section

```java
public class Section {
    private final String id;
    private final String sectionName;  // e.g., "CS-A"
    private final String branch;        // e.g., "Computer Science"
    private final int studentStrength;
    private final int semesterYear;
    private final Set<String> assignedSubjects;

    // Usage
    public static void main(String[] args) {
        Section csA = new Section(
            "SEC001", "CS-A", "CS", 60, 3
        );

        csA.addSubject("SUB001");  // Data Structures
        csA.addSubject("SUB002");  // Database
        csA.addSubject("SUB003");  // Web Dev

        System.out.println(csA.getAssignedSubjectCount()); // 3
        System.out.println(csA.hasSubject("SUB001"));      // true
    }
}
```

---

### Step 1.6: Complete Model Diagram

```
TimeSlot
  - day: DayOfWeek
  - startTime: LocalTime
  - endTime: LocalTime
  + overlaps(TimeSlot): boolean

Subject
  - id, name, department
  - lectureHours, tutorialHours, practicalHours, credits
  + getTotalHours(): int
  + isRequiresLab(): boolean

Space (Abstract)
  - id, name, capacity, status
  - blockedTimeSlots
  + bookTimeSlot(TimeSlot): void
  + isFreeAt(TimeSlot): boolean
  + getSpaceType(): String (abstract)
  + canAccommodateActivityType(ActivityType): boolean (abstract)
  ├─→ LectureHall
  ├─→ ComputerLab
  └─→ Auditorium (future)

Faculty
  - id, name, email, department, maxHoursPerWeek
  - assignedHoursThisWeek (private!)
  - workloadStatus (derived)
  + assignHours(int): void
  + canTakeHours(int): boolean
  + getWorkloadPercentage(): double

Section
  - id, sectionName, branch, studentStrength, semesterYear
  - assignedSubjects: Set
  + addSubject(String): void
  + hasSubject(String): boolean
  + getAssignedSubjectCount(): int

ScheduleSlot
  - id, section, subject, faculty, room, timeSlot, activityType
  - conflictStatus
  + hasConflicts(): boolean

Timetable
  - id, departmentName, semester
  - scheduledSlots: List
  - conflictingSlots: List
  + addScheduleSlot(ScheduleSlot): void
  + getSlotsBySection(String): List
  + getTotalConflictCount(): int
  + isValid(): boolean
```

---

## PHASE 2: SCHEDULING ALGORITHM

### Objective
Implement a Constraint Satisfaction Problem (CSP) solver for automatic timetable generation.

### Key Algorithm: Backtracking with Constraints

```java
public class SchedulingAlgorithm {
    
    public Timetable generateTimetable() {
        // 1. Reset state
        generatedTimetable.reset();
        resetFacultyWorkload();
        resetRoomBookings();

        // 2. Main scheduling loop
        for (Section section : sections) {
            for (String subjectId : section.getAssignedSubjects()) {
                Subject subject = subjectMap.get(subjectId);
                
                // Schedule lecture hours
                scheduleActivities(section, subject, subject.getLectureHours(), 
                    ActivityType.LECTURE);
                
                // Schedule tutorials
                scheduleActivities(section, subject, subject.getTutorialHours(), 
                    ActivityType.TUTORIAL);
                
                // Schedule practicals
                if (subject.isRequiresLab()) {
                    scheduleActivities(section, subject, subject.getPracticalHours(), 
                        ActivityType.PRACTICAL);
                }
            }
        }

        generatedTimetable.markAsGenerated();
        return generatedTimetable;
    }

    private boolean scheduleActivities(Section section, Subject subject, 
                                       int hours, ActivityType type) {
        for (int i = 0; i < hours; i++) {
            // Find best resources
            Faculty faculty = findBestFaculty(subject);
            if (faculty == null) {
                return false;  // No faculty with expertise
            }

            Space room = findAvailableRoom(section, type);
            if (room == null) {
                return false;  // No suitable room
            }

            TimeSlot slot = findAvailableTimeSlot(faculty, room);
            if (slot == null) {
                return false;  // No free time slot
            }

            // Book resources
            ScheduleSlot scheduleSlot = new ScheduleSlot(
                generateSlotId(), section, subject, faculty, room, slot, type
            );

            room.bookTimeSlot(slot);
            faculty.assignHours(1);
            generatedTimetable.addScheduleSlot(scheduleSlot);

            addSuccessLog(scheduleSlot);
        }
        return true;
    }
}
```

### Testing the Algorithm

```java
public static void main(String[] args) {
    // Create sample data
    List<Faculty> faculties = Arrays.asList(
        createFaculty("FAC001", "Dr. Kumar", new HashSet<>(
            Arrays.asList("Data Structures", "Algorithms")
        )),
        createFaculty("FAC002", "Prof. Sharma", new HashSet<>(
            Arrays.asList("Database", "SQL")
        ))
    );

    List<Subject> subjects = Arrays.asList(
        new Subject("SUB001", "Data Structures", "CS", 3, 1, 2, 4),
        new Subject("SUB002", "Database", "CS", 3, 1, 2, 4)
    );

    List<Section> sections = Arrays.asList(
        createSection("SEC001", "CS-A", 60, 
            new HashSet<>(Arrays.asList("SUB001", "SUB002")))
    );

    List<Space> spaces = Arrays.asList(
        new LectureHall("ROOM001", "LH-101", 100),
        new ComputerLab("ROOM002", "Lab-201", 50)
    );

    // Generate
    SchedulingAlgorithm algorithm = new SchedulingAlgorithm(
        sections, faculties, spaces, generateTimeSlots(), subjects
    );

    Timetable timetable = algorithm.generateTimetable();

    // Verify
    System.out.println("Total slots: " + timetable.getScheduledSlots().size());
    System.out.println("Conflicts: " + timetable.getTotalConflictCount());
    System.out.println("Valid: " + timetable.isValid());
}
```

---

## PHASE 3: DATABASE & API

### Step 3.1: Initialize Database

```bash
# Maven build
mvn clean install

# Run with sample data
mvn exec:java -Dexec.mainClass="com.uniplanner.Main"
```

### Step 3.2: Access APIs

```bash
# Get all faculties
curl http://localhost:8080/api/faculty

# Generate timetable
curl -X POST http://localhost:8080/api/timetable/generate

# Get specifics
curl http://localhost:8080/api/dashboard/stats
```

---

## PHASE 4: FRONTEND INTEGRATION

### Step 4.1: Open Dashboard

Navigate to: `http://localhost:8080/index.html`

### Step 4.2: Create Resources

1. Add Faculty via UI
2. Add Subjects
3. Create Sections
4. Add Rooms
5. Click "Generate Timetable"

### Step 4.3: View Results

- Dashboard shows statistics
- Conflicts tab lists problems
- Export button creates Excel/PDF

---

## TESTING & DEBUGGING

### Unit Testing Example

```java
public class FacultyTest {
    @Test
    public void testWorkloadTracking() {
        Faculty faculty = new Faculty(
            "FAC001", "Dr. Kumar", "email@test.com", "CS", 20
        );

        assertEquals(WorkloadStatus.GREEN, faculty.getWorkloadStatus());

        faculty.assignHours(10);
        assertEquals(WorkloadStatus.AMBER, faculty.getWorkloadStatus());

        faculty.assignHours(10);
        assertEquals(WorkloadStatus.RED,faculty.getWorkloadStatus());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCapacityExceeded() {
        Faculty faculty = new Faculty(
            "FAC001", "Dr. Kumar", "email@test.com", "CS", 20
        );
        faculty.assignHours(25);  // Should throw!
    }
}
```

---

## DEPLOYMENT

### Option 1: Standalone JAR
```bash
mvn package
java -jar target/UniPlanner-1.0.0.jar
```

### Option 2: Docker
```dockerfile
FROM openjdk:11
COPY target/UniPlanner-1.0.0.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

**Next Steps:** Extend with your own room types, add new constraints, or integrate with actual university databases!
