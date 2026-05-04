# UniPlanner — Intelligent Multi-Section Timetable Management System

> **Status**: ✅ Fully Operational | Port: `8082` | Java 17 + Spring Boot 3.0

---

## 🚀 Quick Start

```bash
# Compile
mvn compile -DskipTests

# Run
mvn spring-boot:run -DskipTests
```

App starts at: **http://localhost:8082**  
API base: **http://localhost:8082/api**

---

## 🏗️ Architecture

```
src/main/java/com/uniplanner/
├── Main.java                          # Spring Boot entry point
├── UniPlannerController.java          # @Service — orchestrates all operations
├── algorithm/
│   └── SchedulingAlgorithm.java       # CSP timetable generation engine
├── api/
│   ├── ApiController.java             # REST endpoints (faculty/subject/section/room)
│   └── ExportController.java          # Excel + CSV export endpoints
├── models/
│   ├── Schedulable.java               # Interface (OOP: abstraction)
│   ├── Space.java                     # Abstract class (OOP: abstraction)
│   ├── LectureHall.java               # extends Space (OOP: inheritance)
│   ├── ComputerLab.java               # extends Space (OOP: inheritance)
│   ├── SpaceFactory.java              # Factory Pattern (OOP: creational)
│   ├── Faculty.java                   # implements Schedulable, Comparable
│   ├── Subject.java                   # Subject entity
│   ├── Section.java                   # Student group entity
│   ├── TimeSlot.java                  # Immutable time block
│   ├── ScheduleSlot.java              # Single scheduled class entry
│   └── Timetable.java                 # Full schedule container
└── persistence/
    └── DatabaseManager.java           # SQLite DAO (WAL mode, single connection)
```

---

## 🧠 OOP Concepts Implemented

| Concept | Where |
|---------|-------|
| **Abstraction** (Interface) | `Schedulable` — implemented by `Faculty` and `Space` |
| **Abstraction** (Abstract Class) | `Space` — base for `LectureHall` and `ComputerLab` |
| **Inheritance** | `LectureHall` and `ComputerLab` extend `Space` |
| **Polymorphism** | `canAccommodateActivityType()` behaves differently per room type |
| **Encapsulation** | Private fields, unmodifiable collection views, controlled setters |
| **Factory Pattern** | `SpaceFactory.createSpace()` / `createLectureHall()` / `createComputerLab()` |
| **Static Factory Method** | `ScheduleSlot.createConflictSlot()` — bypasses validation for conflict tracking |
| **Comparable** | `Faculty` and `Space` sortable by workload/utilization |
| **Enum with Behavior** | `WorkloadStatus`, `RoomStatus`, `ConflictStatus` — each with descriptions |
| **Composition** | `Timetable` contains `List<ScheduleSlot>` |
| **Singleton** | `DatabaseManager` — single connection via Spring `@Component` |
| **MVC** | `ApiController` (controller) ↔ `UniPlannerController` (service) ↔ models |

---

## 📡 API Reference

### Dashboard
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/dashboard/stats` | Faculty count, room utilization, conflicts |
| GET | `/api/dashboard/utilization` | Average workload and room usage percentages |

### Faculty
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/faculty` | List all faculty |
| POST | `/api/faculty` | Add faculty |
| DELETE | `/api/faculty/{id}` | Remove faculty |
| GET | `/api/faculty/{id}/workload` | Workload stats for faculty |

### Subjects
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/subject` | List all subjects |
| POST | `/api/subject` | Add subject (with lecture/tutorial/practical hours) |
| DELETE | `/api/subject/{id}` | Remove subject |

### Sections
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/section` | List all sections |
| POST | `/api/section` | Add section |
| DELETE | `/api/section/{id}` | Remove section |

### Rooms
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/room` | List all rooms |
| POST | `/api/room` | Add room (`spaceType`: `LectureHall` or `ComputerLab`) |
| PUT | `/api/room/{id}/status` | Toggle Active / Maintenance |

### Timetable
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/timetable/generate` | Run CSP algorithm, generate full timetable |
| GET | `/api/timetable/current` | Get current timetable with all slots |
| GET | `/api/timetable/section/{id}` | Get timetable for one section |
| GET | `/api/timetable/conflicts` | List conflicting slots |

### Export ✅
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/timetable/export/excel` | Full timetable as `.xlsx` (styled, 2 sheets) |
| GET | `/api/timetable/export/excel/{sectionId}` | Single section as `.xlsx` |
| GET | `/api/timetable/export/csv` | Full timetable as `.csv` |
| GET | `/api/timetable/export/csv/{sectionId}` | Single section as `.csv` |

---

## 📊 Sample Data (Auto-loaded on startup)

### Faculty (8)
| ID | Name | Department | Expertise |
|----|------|-----------|-----------|
| FAC001 | Dr. Rajesh Kumar | CS | Data Structures, Algorithms |
| FAC002 | Prof. Suresh Sharma | CS | Database Management |
| FAC003 | Dr. Priya Mehta | CS | Operating Systems, Networks |
| FAC004 | Prof. Amit Singh | CS | OOP, Java |
| FAC005 | Dr. Neha Verma | EC | Digital Electronics, VLSI |
| FAC006 | Prof. Vikram Joshi | EC | Signal Processing |
| FAC007 | Dr. Rahul Gupta | ME | Thermodynamics |
| FAC008 | Prof. Sanjay Patel | ME | Engineering Mechanics |

### Sections (6)
`CS-A`, `CS-B`, `CS-C` (CS dept) | `EC-A`, `EC-B` (EC dept) | `ME-A` (ME dept)

### Rooms (8)
- **Lecture Halls**: LH-101 (100), LH-102 (80), LH-201 (120), LH-202 (70)
- **Computer Labs**: CS-Lab-1 (60), CS-Lab-2 (50), EC-Lab-1 (55), ME-Lab-1 (60)

---

## 🔒 Constraint Satisfaction Algorithm

**Hard constraints** (must be satisfied):
1. No faculty double-booking at same time slot
2. No room double-booking at same time slot
3. Room type must match activity (LectureHall → LECTURE/TUTORIAL; Lab → PRACTICAL/TUTORIAL)
4. Room capacity ≥ section student count
5. Faculty must have expertise in subject

**Soft constraints** (optimized):
1. Prefer faculty with lower workload (`GREEN` < `AMBER` < `RED`)
2. Prefer rooms with lower utilization (balanced usage)

---

## 🧪 Running Tests

```bash
mvn test
```

3 tests in `SchedulingAlgorithmTest.java`:
- `generateTimetableShouldWorkOnRepeatedRuns` — idempotency check
- `generateTimetableShouldSupportPartialFacultyExpertiseMatches` — fuzzy expertise matching
- `generateTimetableShouldHandleNoAvailableFacultyGracefully` — conflict handling

---

## 🗄️ Database

- **Engine**: SQLite (`./timetable.db`)
- **Mode**: WAL (Write-Ahead Logging) for concurrent access
- **Tables**: `faculty`, `faculty_expertise`, `subject`, `section`, `section_subjects`, `space`, `time_slot`
- > **Note**: Delete `timetable.db` before first run if switching from the old version

---

## ⚙️ Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Java 17, Spring Boot 3.0 |
| REST API | Spring MVC (`@RestController`) |
| Database | SQLite via `sqlite-jdbc 3.41` |
| Excel Export | Apache POI 5.2.3 |
| JSON | Jackson (Spring Boot default) |
| Frontend | HTML5, CSS3, Vanilla JS |
| Build | Maven |
