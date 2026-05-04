# OOP Principles in UniPlanner

> A deep dive into Object-Oriented Programming concepts demonstrated in the UniPlanner project

## TABLE OF CONTENTS

1. [Abstraction](#abstraction)
2. [Inheritance](#inheritance)
3. [Polymorphism](#polymorphism)
4. [Encapsulation](#encapsulation)
5. [Design Patterns](#design-patterns)

---

## ABSTRACTION

### Definition

Abstraction means hiding complex implementation details and showing only the necessary features of an object.

### Implementation: `Space` Abstract Class

```java
public abstract class Space {
    // Common properties all rooms share
    protected final String id;
    protected final String name;
    protected final int capacity;
    protected RoomStatus status;
    protected final List<TimeSlot> blockedTimeSlots;

    // Concrete methods (shared behavior)
    public void bookTimeSlot(TimeSlot slot) { ... }
    public boolean isFreeAt(TimeSlot slot) { ... }
    public double getUtilizationPercentage() { ... }

    // Abstract methods (must be implemented by subclasses)
    public abstract String getSpaceType();
    public abstract boolean canAccommodateActivityType(ActivityType type);
}
```

### Why It Matters

- **Abstraction hides complexity** - Clients don't need to know HOW a room books a slot, just THAT it does
- **Defines a contract** - All rooms MUST implement `getSpaceType()` and `canAccommodateActivityType()`
- **Enables extensibility** - Can easily add new room types (Auditorium, Seminar Hall) without changing existing code

### Real-World Analogy

Think of a coffee maker interface:

- **What you see**: Button to make coffee, display of status
- **What you don't see**: Internal heating mechanism, water pressure system
- That's abstraction - showing only what matters to the user

---

## INHERITANCE

### Definition

Inheritance allows a class to acquire properties and methods from another class. It promotes code reuse and establishes relationships.

### Implementation: `LectureHall` and `ComputerLab` inherit from `Space`

```java
// Parent Class
public abstract class Space {
    // Shared properties and methods
}

// Child Class #1
public class LectureHall extends Space {
    private final boolean hasProjector;
    private final boolean hasWhiteboard;
    private final boolean hasAirConditioning;

    @Override
    public String getSpaceType() {
        return "LectureHall";
    }

    @Override
    public boolean canAccommodateActivityType(ActivityType activityType) {
        return activityType == ActivityType.LECTURE ||
               activityType == ActivityType.TUTORIAL;
    }
}

// Child Class #2
public class ComputerLab extends Space {
    private final int numberOfSystems;
    private final String operatingSystem;

    @Override
    public String getSpaceType() {
        return "ComputerLab";
    }

    @Override
    public boolean canAccommodateActivityType(ActivityType activityType) {
        return activityType == ActivityType.PRACTICAL ||
               activityType == ActivityType.TUTORIAL;
    }

    public boolean hasSufficientSystems(int studentCount) {
        return numberOfSystems >= studentCount;
    }
}
```

### Benefits

| Benefit                      | Example                                                                            |
| ---------------------------- | ---------------------------------------------------------------------------------- |
| **Code Reuse**               | Both LectureHall and ComputerLab inherit `bookTimeSlot()`, `isFreeAt()` from Space |
| **Hierarchy & Organization** | Clear classification: Space → LectureHall/ComputerLab                              |
| **Consistency**              | All rooms have same interface for booking, availability checking                   |
| **Maintainability**          | Modify Space base class, all children benefit                                      |

### Inheritance Chain

```
    Object (Java)
      ↓
    Space (Abstract Base)
      ├──→ LectureHall
      ├──→ ComputerLab
      └──→ Auditorium (Future extension)
```

---

## POLYMORPHISM

### Definition

Polymorphism means "many forms." The ability of objects to take multiple forms or for a method to behave differently based on context.

### Type 1: Method Overriding (Runtime Polymorphism)

```java
// List of different space types
List<Space> spaces = Arrays.asList(
    new LectureHall("LH101", "Lecture Hall", 100),
    new ComputerLab("LAB201", "Computer Lab", 50),
    new LectureHall("LH102", "Lecture Hall", 80)
);

// Polymorphic behavior - same method, different results!
for (Space space : spaces) {
    // Each space implements differently
    space.canAccommodateActivityType(ActivityType.PRACTICAL);
    // LectureHall → false
    // ComputerLab → true
    // LectureHall → false
}
```

**Real Value:**

```java
// Without Polymorphism (😞 Bad)
if (space instanceof LectureHall) {
    LectureHall lh = (LectureHall) space;
    return lh.canAccommodateActivityType(type);
} else if (space instanceof ComputerLab) {
    ComputerLab lab = (ComputerLab) space;
    return lab.canAccommodateActivityType(type);
}

// With Polymorphism (✅ Good)
return space.canAccommodateActivityType(type); // Works for any Space!
```

### Type 2: Method Overloading (Compile-Time Polymorphism)

```java
public class TimeSlot {
    // Same method name, different parameters
    public TimeSlot(DayOfWeek day, LocalTime start, LocalTime end) { ... }

    // Overloaded version
    public boolean overlaps(TimeSlot other) { ... }
}

public class Faculty {
    // Overloaded method
    public boolean canTakeHours(int hours) {
        return (assignedHoursThisWeek + hours) <= maxHoursPerWeek;
    }
}
```

### Why Polymorphism Matters

1. **Flexibility** - Write algorithms that work with ANY Space type
2. **Extensibility** - Add new room types without changing existing code
3. **Maintainability** - Less conditional logic (if/else chains)
4. **Real-world modeling** - Reflects how objects truly behave

---

## ENCAPSULATION

### Definition

Encapsulation is bundling data (variables) and behavior (methods) into a single unit (class) while hiding implementation details. Data is typically private, accessed via public methods.

### Implementation: `Faculty` Class

```java
public class Faculty {
    // Private data - HIDDEN from outside access
    private final String id;
    private final String name;
    private final String email;
    private final String department;
    private int assignedHoursThisWeek;  // ← Can change!
    private WorkloadStatus workloadStatus;

    // Private constructor parameters validation
    public Faculty(String id, String name, String email, String department, int maxHours) {
        if (maxHours <= 0 || maxHours > 50) {
            throw new IllegalArgumentException("Invalid hours");
        }
        this.id = id;
        this.name = name;
        // ... more initialization
    }

    // Public getters - READ-ONLY access to sensitive data
    public String getId() {
        return id;  // Safe to return - String is immutable
    }

    public int getAssignedHoursThisWeek() {
        return assignedHoursThisWeek;  // Nobody can modify directly!
    }

    // Controlled setter - LOGIC VALIDATION
    public void assignHours(int hours) {
        if (!canTakeHours(hours)) {
            throw new IllegalArgumentException("Exceeds capacity");
        }
        this.assignedHoursThisWeek += hours;
        updateWorkloadStatus();  // Side effect: automatically update status
    }

    // Private helper method - HIDDEN implementation
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

    // Constraint checking method
    public boolean canTakeHours(int hours) {
        return (assignedHoursThisWeek + hours) <= maxHoursPerWeek;
    }
}
```

### Encapsulation in Action

```java
// ❌ WITHOUT ENCAPSULATION (Nightmare!)
Faculty f = new Faculty(...);
f.assignedHoursThisWeek = 50;  // INVALID! Exceeds max capacity
f.workloadStatus = INVALID_STATUS;  // Inconsistent state!

// ✅ WITH ENCAPSULATION (Safe!)
Faculty f = new Faculty(...);
f.assignHours(50);  // Validates, throws exception if invalid
// workloadStatus automatically updated
System.out.println(f.getWorkloadPercentage());  // Safe read
```

### Benefits of Encapsulation

| Aspect             | Benefit                                                                |
| ------------------ | ---------------------------------------------------------------------- |
| **Data Integrity** | Private fields can't be directly corrupted                             |
| **Flexibility**    | Can change internal implementation without breaking client code        |
| **Validation**     | Control how data is modified via methods                               |
| **Consistency**    | Related fields stay in sync (e.g., `assignedHours` → `workloadStatus`) |

---

## DESIGN PATTERNS

### Pattern 1: Singleton Pattern (DatabaseManager)

```java
public class DatabaseManager {
    private static DatabaseManager instance;

    private DatabaseManager(String dbPath) {
        this.databaseUrl = "jdbc:sqlite:" + dbPath;
        initializeDatabase();
    }

    // Lazy initialization
    public static synchronized DatabaseManager getInstance(String dbPath) {
        if (instance == null) {
            instance = new DatabaseManager(dbPath);
        }
        return instance;
    }
}

// Usage
DatabaseManager db = DatabaseManager.getInstance("./timetable.db");
```

**Why it matters:** Only ONE database connection throughout the app

---

### Pattern 2: Template Method Pattern (SchedulingAlgorithm)

```java
public class SchedulingAlgorithm {
    // Template: Defines algorithm structure
    public Timetable generateTimetable() {
        generatedTimetable.reset();
        resetFacultyWorkload();
        resetRoomBookings();

        for (Section section : sections) {
            for (String subjectId : section.getAssignedSubjects()) {
                // Flexible: different implementation details
                if (subject.getLectureHours() > 0) {
                    scheduleSubjectActivity(..., ActivityType.LECTURE);
                }
            }
        }
        return generatedTimetable;
    }

    // Flexible method
    private boolean scheduleSubjectActivity(...) { ... }
}
```

---

### Pattern 3: Strategy Pattern (Room Selection)

```java
// Different strategies for selecting a room
private Space findAvailableRoom(Section section, Space.ActivityType activityType) {
    return spaces.stream()
        .filter(Space::isAvailable)
        .filter(space -> space.canAccommodateActivityType(activityType))
        .filter(space -> space.getCapacity() >= section.getStudentStrength())
        // Strategy 1: Prefer least utilized room (soft constraint)
        .min(Comparator.comparingDouble(Space::getUtilizationPercentage))
        .orElse(null);
}

// vs. Strategy 2: Could prefer room closest to previous slot
// vs. Strategy 3: Could prefer room with most amenities
```

---

## COMPARISON: With vs. Without OOP

### Scenario: Add a new room type (Seminar Hall)

#### ❌ WITHOUT OOP (Hard-coded approach)

```java
public class SchedulingEngine {
    public void scheduleClass(String roomType, ...) {
        if (roomType.equals("LectureHall")) {
            // Lecture logic
        } else if (roomType.equals("ComputerLab")) {
            // Lab logic
        } else if (roomType.equals("SeminarHall")) {  // ← NEW CODE
            // Seminar logic
        }
        // Modify 10+ places in codebase!
    }
}
```

#### ✅ WITH OOP (Extensible approach)

```java
// Create ONE new class
public class SeminarHall extends Space {
    @Override
    public boolean canAccommodateActivityType(ActivityType type) {
        return type == ActivityType.TUTORIAL;  // Seminars = tutorials
    }
}

// Everything else works automatically!
spaces.add(new SeminarHall(...));
// SchedulingAlgorithm doesn't need ANY changes
```

---

## LEARNING CHECKLIST

- [ ] Understand why `Space` is abstract
- [ ] Explain inheritance: LectureHall **IS-A** Space
- [ ] Describe polymorphic behavior of `canAccommodateActivityType()`
- [ ] Explain why `Faculty.assignedHoursThisWeek` is private
- [ ] Implement your own room type (Auditorium) extending Space
- [ ] Modify `SchedulingAlgorithm` to use new room type (no other changes needed!)

---

## CONCLUSION

UniPlanner demonstrates professional OOP design:

- **Abstraction** hides complexity (Space abstract class)
- **Inheritance** promotes code reuse (LectureHall, ComputerLab)
- **Polymorphism** enables flexibility (method overriding)
- **Encapsulation** ensures data integrity (private fields, controlled access)

These principles make the code:

- **Maintainable** - Easy to modify
- **Extensible** - Easy to add features
- **Scalable** - Easy to handle growth
- **Professional** - Industry-standard practices

---

**Next:** Review [IMPLEMENTATION_GUIDE.md](./IMPLEMENTATION_GUIDE.md) for hands-on coding examples
