package com.uniplanner.persistence;

import com.uniplanner.models.*;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.Closeable;
import java.sql.*;
import java.util.*;

/**
 * ========================================================================
 * OOP CONCEPTS: SINGLETON PATTERN + INTERFACE IMPLEMENTATION + ENCAPSULATION
 * ========================================================================
 * Database Access Object (DAO) for SQLite persistence.
 *
 * - SINGLETON PATTERN: Only one instance manages the DB connection
 * - INTERFACE: Implements Closeable for resource management
 * - ENCAPSULATION: Private connection, public CRUD methods
 * - FACTORY usage: Uses SpaceFactory to reconstruct Space objects from DB
 * ========================================================================
 */
@Component
public class DatabaseManager implements Closeable {

    // ENCAPSULATION: Private database connection
    private Connection connection;
    private final String databaseUrl;

    /**
     * SINGLETON: Spring @Component ensures single instance.
     * Constructor accepts DB path for flexibility.
     */
    public DatabaseManager() {
        this.databaseUrl = "jdbc:sqlite:./timetable.db";
    }

    public DatabaseManager(String dbPath) {
        this.databaseUrl = "jdbc:sqlite:" + dbPath;
    }

    // SINGLETON: Static factory for non-Spring usage
    private static DatabaseManager instance;

    public static synchronized DatabaseManager getInstance(String dbPath) {
        if (instance == null) {
            instance = new DatabaseManager(dbPath);
            instance.initializeDatabase();
        }
        return instance;
    }

    /**
     * Get or create a reusable database connection.
     * Uses WAL mode for better concurrency on Windows/OneDrive.
     */
    private synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(databaseUrl);
            // Enable WAL mode for better concurrent access
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("PRAGMA journal_mode=WAL");
                stmt.execute("PRAGMA busy_timeout=5000");
            }
        }
        return connection;
    }

    /**
     * Initialize database schema
     */
    @PostConstruct
    public void initializeDatabase() {
        try {
            Connection conn = getConnection();
            try (Statement stmt = conn.createStatement()) {

                // Create Faculty table
                stmt.execute("CREATE TABLE IF NOT EXISTS faculty (" +
                        "id TEXT PRIMARY KEY," +
                        "name TEXT NOT NULL," +
                        "email TEXT NOT NULL," +
                        "department TEXT NOT NULL," +
                        "max_hours_per_week INTEGER NOT NULL," +
                        "assigned_hours_this_week INTEGER DEFAULT 0," +
                        "workload_status TEXT DEFAULT 'GREEN'," +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                        ")");

                // Create Subject table
                stmt.execute("CREATE TABLE IF NOT EXISTS subject (" +
                        "id TEXT PRIMARY KEY," +
                        "name TEXT NOT NULL," +
                        "department TEXT NOT NULL," +
                        "lecture_hours INTEGER DEFAULT 0," +
                        "tutorial_hours INTEGER DEFAULT 0," +
                        "practical_hours INTEGER DEFAULT 0," +
                        "credit_weightage INTEGER DEFAULT 0," +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                        ")");

                // Create Space (Room) table
                stmt.execute("CREATE TABLE IF NOT EXISTS space (" +
                        "id TEXT PRIMARY KEY," +
                        "name TEXT NOT NULL," +
                        "capacity INTEGER NOT NULL," +
                        "space_type TEXT NOT NULL," +
                        "status TEXT DEFAULT 'ACTIVE'," +
                        "additional_info TEXT," +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                        ")");

                // Create Section table
                stmt.execute("CREATE TABLE IF NOT EXISTS section (" +
                        "id TEXT PRIMARY KEY," +
                        "section_name TEXT NOT NULL," +
                        "branch TEXT NOT NULL," +
                        "student_strength INTEGER NOT NULL," +
                        "semester_year INTEGER NOT NULL," +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                        ")");

                // Create TimeSlot table
                stmt.execute("CREATE TABLE IF NOT EXISTS time_slot (" +
                        "id TEXT PRIMARY KEY," +
                        "day_of_week TEXT NOT NULL," +
                        "start_time TEXT NOT NULL," +
                        "end_time TEXT NOT NULL," +
                        "duration_minutes INTEGER NOT NULL" +
                        ")");

                // Create Faculty Expertise table
                stmt.execute("CREATE TABLE IF NOT EXISTS faculty_expertise (" +
                        "faculty_id TEXT NOT NULL," +
                        "expertise_area TEXT NOT NULL," +
                        "PRIMARY KEY(faculty_id, expertise_area)," +
                        "FOREIGN KEY(faculty_id) REFERENCES faculty(id)" +
                        ")");

                // Create Section Subjects table
                stmt.execute("CREATE TABLE IF NOT EXISTS section_subjects (" +
                        "section_id TEXT NOT NULL," +
                        "subject_id TEXT NOT NULL," +
                        "PRIMARY KEY(section_id, subject_id)," +
                        "FOREIGN KEY(section_id) REFERENCES section(id)," +
                        "FOREIGN KEY(subject_id) REFERENCES subject(id)" +
                        ")");

                System.out.println("[DB] Database initialized successfully at: " + databaseUrl);
            }

        } catch (SQLException e) {
            System.err.println("[DB] Error initializing database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * INTERFACE IMPLEMENTATION: Closeable — clean resource release
     */
    @PreDestroy
    @Override
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("[DB] Database connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("[DB] Error closing connection: " + e.getMessage());
        }
    }

    // ============== FACULTY OPERATIONS ==============

    public void saveFaculty(Faculty faculty) {
        String sql = "INSERT OR REPLACE INTO faculty (id, name, email, department, max_hours_per_week, assigned_hours_this_week, workload_status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try {
            Connection conn = getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, faculty.getId());
                pstmt.setString(2, faculty.getName());
                pstmt.setString(3, faculty.getEmail());
                pstmt.setString(4, faculty.getDepartment());
                pstmt.setInt(5, faculty.getMaxHoursPerWeek());
                pstmt.setInt(6, faculty.getAssignedHoursThisWeek());
                pstmt.setString(7, faculty.getWorkloadStatus().toString());
                pstmt.executeUpdate();
            }

            // Save expertise areas
            try (PreparedStatement deleteStmt = conn.prepareStatement(
                    "DELETE FROM faculty_expertise WHERE faculty_id = ?")) {
                deleteStmt.setString(1, faculty.getId());
                deleteStmt.executeUpdate();
            }

            try (PreparedStatement insertStmt = conn.prepareStatement(
                    "INSERT OR IGNORE INTO faculty_expertise (faculty_id, expertise_area) VALUES (?, ?)")) {
                for (String expertise : faculty.getExpertiseAreas()) {
                    insertStmt.setString(1, faculty.getId());
                    insertStmt.setString(2, expertise);
                    insertStmt.addBatch();
                }
                insertStmt.executeBatch();
            }

        } catch (SQLException e) {
            System.err.println("[DB] Error saving faculty: " + e.getMessage());
        }
    }

    public List<Faculty> getAllFaculties() {
        List<Faculty> faculties = new ArrayList<>();
        try {
            Connection conn = getConnection();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM faculty")) {

                while (rs.next()) {
                    Faculty faculty = new Faculty(
                            rs.getString("id"),
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getString("department"),
                            rs.getInt("max_hours_per_week"));

                    // Load expertise areas
                    try (PreparedStatement pstmt = conn.prepareStatement(
                            "SELECT expertise_area FROM faculty_expertise WHERE faculty_id = ?")) {
                        pstmt.setString(1, faculty.getId());
                        try (ResultSet expertiseRs = pstmt.executeQuery()) {
                            while (expertiseRs.next()) {
                                faculty.addExpertise(expertiseRs.getString("expertise_area"));
                            }
                        }
                    }
                    faculties.add(faculty);
                }
            }
        } catch (SQLException e) {
            System.err.println("[DB] Error retrieving faculties: " + e.getMessage());
        }
        return faculties;
    }

    public void deleteFaculty(String id) {
        try {
            Connection conn = getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement("DELETE FROM faculty_expertise WHERE faculty_id = ?")) {
                pstmt.setString(1, id);
                pstmt.executeUpdate();
            }
            try (PreparedStatement pstmt = conn.prepareStatement("DELETE FROM faculty WHERE id = ?")) {
                pstmt.setString(1, id);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("[DB] Error deleting faculty: " + e.getMessage());
        }
    }

    // ============== SUBJECT OPERATIONS ==============

    public void saveSubject(Subject subject) {
        String sql = "INSERT OR REPLACE INTO subject (id, name, department, lecture_hours, tutorial_hours, practical_hours, credit_weightage) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setString(1, subject.getId());
            pstmt.setString(2, subject.getName());
            pstmt.setString(3, subject.getDepartment());
            pstmt.setInt(4, subject.getLectureHours());
            pstmt.setInt(5, subject.getTutorialHours());
            pstmt.setInt(6, subject.getPracticalHours());
            pstmt.setInt(7, subject.getCreditWeightage());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DB] Error saving subject: " + e.getMessage());
        }
    }

    public List<Subject> getAllSubjects() {
        List<Subject> subjects = new ArrayList<>();
        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM subject")) {

            while (rs.next()) {
                subjects.add(new Subject(
                        rs.getString("id"),
                        rs.getString("name"),
                        rs.getString("department"),
                        rs.getInt("lecture_hours"),
                        rs.getInt("tutorial_hours"),
                        rs.getInt("practical_hours"),
                        rs.getInt("credit_weightage")));
            }
        } catch (SQLException e) {
            System.err.println("[DB] Error retrieving subjects: " + e.getMessage());
        }
        return subjects;
    }

    public void deleteSubject(String id) {
        try (PreparedStatement pstmt = getConnection().prepareStatement("DELETE FROM subject WHERE id = ?")) {
            pstmt.setString(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DB] Error deleting subject: " + e.getMessage());
        }
    }

    // ============== SECTION OPERATIONS ==============

    public void saveSection(Section section) {
        try {
            Connection conn = getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT OR REPLACE INTO section (id, section_name, branch, student_strength, semester_year) VALUES (?, ?, ?, ?, ?)")) {
                pstmt.setString(1, section.getId());
                pstmt.setString(2, section.getSectionName());
                pstmt.setString(3, section.getBranch());
                pstmt.setInt(4, section.getStudentStrength());
                pstmt.setInt(5, section.getSemesterYear());
                pstmt.executeUpdate();
            }

            // Save assigned subjects
            try (PreparedStatement deleteStmt = conn.prepareStatement(
                    "DELETE FROM section_subjects WHERE section_id = ?")) {
                deleteStmt.setString(1, section.getId());
                deleteStmt.executeUpdate();
            }

            try (PreparedStatement insertStmt = conn.prepareStatement(
                    "INSERT OR IGNORE INTO section_subjects (section_id, subject_id) VALUES (?, ?)")) {
                for (String subjectId : section.getAssignedSubjects()) {
                    insertStmt.setString(1, section.getId());
                    insertStmt.setString(2, subjectId);
                    insertStmt.addBatch();
                }
                insertStmt.executeBatch();
            }
        } catch (SQLException e) {
            System.err.println("[DB] Error saving section: " + e.getMessage());
        }
    }

    public List<Section> getAllSections() {
        List<Section> sections = new ArrayList<>();
        try {
            Connection conn = getConnection();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM section")) {

                while (rs.next()) {
                    Section section = new Section(
                            rs.getString("id"),
                            rs.getString("section_name"),
                            rs.getString("branch"),
                            rs.getInt("student_strength"),
                            rs.getInt("semester_year"));

                    // Load assigned subjects
                    try (PreparedStatement pstmt = conn.prepareStatement(
                            "SELECT subject_id FROM section_subjects WHERE section_id = ?")) {
                        pstmt.setString(1, section.getId());
                        try (ResultSet subjectRs = pstmt.executeQuery()) {
                            while (subjectRs.next()) {
                                section.addSubject(subjectRs.getString("subject_id"));
                            }
                        }
                    }
                    sections.add(section);
                }
            }
        } catch (SQLException e) {
            System.err.println("[DB] Error retrieving sections: " + e.getMessage());
        }
        return sections;
    }

    public void deleteSection(String id) {
        try {
            Connection conn = getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement("DELETE FROM section_subjects WHERE section_id = ?")) {
                pstmt.setString(1, id);
                pstmt.executeUpdate();
            }
            try (PreparedStatement pstmt = conn.prepareStatement("DELETE FROM section WHERE id = ?")) {
                pstmt.setString(1, id);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("[DB] Error deleting section: " + e.getMessage());
        }
    }

    // ============== SPACE (ROOM) OPERATIONS ==============

    public void saveSpace(Space space) {
        String sql = "INSERT OR REPLACE INTO space (id, name, capacity, space_type, status, additional_info) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setString(1, space.getId());
            pstmt.setString(2, space.getName());
            pstmt.setInt(3, space.getCapacity());
            pstmt.setString(4, space.getSpaceType());
            pstmt.setString(5, space.getStatus().toString());
            // POLYMORPHISM: instanceof checks the runtime type
            pstmt.setString(6, space instanceof ComputerLab ? "LAB" : "LECTURE_HALL");
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DB] Error saving space: " + e.getMessage());
        }
    }

    public List<Space> getAllSpaces() {
        List<Space> spaces = new ArrayList<>();
        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM space")) {

            while (rs.next()) {
                String id = rs.getString("id");
                String name = rs.getString("name");
                int capacity = rs.getInt("capacity");
                String additionalInfo = rs.getString("additional_info");

                // FACTORY PATTERN: Use SpaceFactory to create the correct subclass
                Space space;
                if ("LAB".equals(additionalInfo)) {
                    space = SpaceFactory.createComputerLab(id, name, capacity, capacity, "Windows");
                } else {
                    space = SpaceFactory.createLectureHall(id, name, capacity, true, true, false);
                }
                spaces.add(space);
            }
        } catch (SQLException e) {
            System.err.println("[DB] Error retrieving spaces: " + e.getMessage());
        }
        return spaces;
    }

    public void deleteSpace(String id) {
        try (PreparedStatement pstmt = getConnection().prepareStatement("DELETE FROM space WHERE id = ?")) {
            pstmt.setString(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DB] Error deleting space: " + e.getMessage());
        }
    }

    // ============== TIME SLOT OPERATIONS ==============

    public void saveTimeSlot(TimeSlot timeSlot) {
        String sql = "INSERT OR IGNORE INTO time_slot (id, day_of_week, start_time, end_time, duration_minutes) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            String id = timeSlot.getDay() + "_" + timeSlot.getStartTime() + "_" + timeSlot.getEndTime();
            pstmt.setString(1, id);
            pstmt.setString(2, timeSlot.getDay().toString());
            pstmt.setString(3, timeSlot.getStartTime().toString());
            pstmt.setString(4, timeSlot.getEndTime().toString());
            pstmt.setInt(5, timeSlot.getDurationMinutes());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DB] Error saving time slot: " + e.getMessage());
        }
    }

    public List<TimeSlot> getAllTimeSlots() {
        List<TimeSlot> timeSlots = new ArrayList<>();
        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM time_slot")) {

            while (rs.next()) {
                timeSlots.add(new TimeSlot(
                        TimeSlot.DayOfWeek.valueOf(rs.getString("day_of_week")),
                        java.time.LocalTime.parse(rs.getString("start_time")),
                        java.time.LocalTime.parse(rs.getString("end_time"))
                ));
            }
        } catch (SQLException e) {
            System.err.println("[DB] Error retrieving time slots: " + e.getMessage());
        }
        return timeSlots;
    }
}
