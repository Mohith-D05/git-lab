package com.uniplanner.api;

import com.uniplanner.UniPlannerController;
import com.uniplanner.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * ========================================================================
 * Spring REST Controller for UniPlanner API
 * ========================================================================
 * Maps HTTP endpoints to timetable management operations.
 * Uses Spring Dependency Injection (@Autowired) to get the controller.
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ApiController {

    // DEPENDENCY INJECTION: Spring injects the singleton UniPlannerController
    private final UniPlannerController controller;

    @Autowired
    public ApiController(UniPlannerController controller) {
        this.controller = controller;
    }

    // ============== FACULTY ENDPOINTS ==============

    @GetMapping("/faculty")
    public ResponseEntity<?> getAllFaculties() {
        try {
            List<Map<String, Object>> response = controller.getFaculties().stream()
                    .map(this::toFacultyResponse)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return errorResponse(e);
        }
    }

    @PostMapping("/faculty")
    public ResponseEntity<?> createFaculty(@RequestBody Map<String, Object> data) {
        try {
            Faculty faculty = new Faculty(
                    getString(data, "id", "FAC_" + System.currentTimeMillis()),
                    getRequired(data, "name"),
                    getRequired(data, "email"),
                    getRequired(data, "department"),
                    getInt(data, "maxHoursPerWeek", 20));
            parseList(data.get("expertiseAreas")).forEach(faculty::addExpertise);
            controller.addFaculty(faculty);
            return ResponseEntity.ok(new ApiResponse(true, "Faculty created successfully", toFacultyResponse(faculty)));
        } catch (Exception e) {
            return errorResponse(e);
        }
    }

    @DeleteMapping("/faculty/{id}")
    public ResponseEntity<?> deleteFaculty(@PathVariable String id) {
        try {
            controller.deleteFaculty(id);
            return ResponseEntity.ok(new ApiResponse(true, "Faculty deleted successfully", null));
        } catch (Exception e) {
            return errorResponse(e);
        }
    }

    @GetMapping("/faculty/{id}/workload")
    public ResponseEntity<?> getFacultyWorkload(@PathVariable String id) {
        try {
            Optional<Faculty> faculty = controller.getFaculties().stream()
                    .filter(f -> f.getId().equals(id)).findFirst();
            if (faculty.isPresent()) {
                Faculty f = faculty.get();
                Map<String, Object> workload = new LinkedHashMap<>();
                workload.put("facultyId", f.getId());
                workload.put("name", f.getName());
                workload.put("assignedHours", f.getAssignedHoursThisWeek());
                workload.put("maxHours", f.getMaxHoursPerWeek());
                workload.put("workloadPercentage", f.getWorkloadPercentage());
                workload.put("workloadStatus", f.getWorkloadStatus().toString());
                workload.put("remainingCapacity", f.getRemainingCapacity());
                return ResponseEntity.ok(new ApiResponse(true, "Workload retrieved", workload));
            }
            return ResponseEntity.ok(new ApiResponse(false, "Faculty not found", null));
        } catch (Exception e) {
            return errorResponse(e);
        }
    }

    // ============== SUBJECT ENDPOINTS ==============

    @GetMapping("/subject")
    public ResponseEntity<?> getAllSubjects() {
        try {
            List<Map<String, Object>> response = controller.getSubjects().stream()
                    .map(this::toSubjectResponse)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return errorResponse(e);
        }
    }

    @PostMapping("/subject")
    public ResponseEntity<?> createSubject(@RequestBody Map<String, Object> data) {
        try {
            Subject subject = new Subject(
                    getString(data, "id", "SUB_" + System.currentTimeMillis()),
                    getRequired(data, "name"),
                    getRequired(data, "department"),
                    getInt(data, "lectureHours", 3),
                    getInt(data, "tutorialHours", 1),
                    getInt(data, "practicalHours", 2),
                    getInt(data, "creditWeightage", 4));
            controller.addSubject(subject);
            return ResponseEntity.ok(new ApiResponse(true, "Subject created successfully", toSubjectResponse(subject)));
        } catch (Exception e) {
            return errorResponse(e);
        }
    }

    @DeleteMapping("/subject/{id}")
    public ResponseEntity<?> deleteSubject(@PathVariable String id) {
        try {
            controller.deleteSubject(id);
            return ResponseEntity.ok(new ApiResponse(true, "Subject deleted successfully", null));
        } catch (Exception e) {
            return errorResponse(e);
        }
    }

    // ============== SECTION ENDPOINTS ==============

    @GetMapping("/section")
    public ResponseEntity<?> getAllSections() {
        try {
            List<Map<String, Object>> response = controller.getSections().stream()
                    .map(this::toSectionResponse)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return errorResponse(e);
        }
    }

    @PostMapping("/section")
    public ResponseEntity<?> createSection(@RequestBody Map<String, Object> data) {
        try {
            Section section = new Section(
                    getString(data, "id", "SEC_" + System.currentTimeMillis()),
                    getRequired(data, "sectionName"),
                    getRequired(data, "branch"),
                    getInt(data, "studentStrength", 50),
                    getInt(data, "semesterYear", 1));
            parseList(data.get("assignedSubjects")).forEach(section::addSubject);
            controller.addSection(section);
            return ResponseEntity.ok(new ApiResponse(true, "Section created successfully", toSectionResponse(section)));
        } catch (Exception e) {
            return errorResponse(e);
        }
    }

    @DeleteMapping("/section/{id}")
    public ResponseEntity<?> deleteSection(@PathVariable String id) {
        try {
            controller.deleteSection(id);
            return ResponseEntity.ok(new ApiResponse(true, "Section deleted successfully", null));
        } catch (Exception e) {
            return errorResponse(e);
        }
    }

    // ============== ROOM ENDPOINTS ==============

    @GetMapping("/room")
    public ResponseEntity<?> getAllRooms() {
        try {
            List<Map<String, Object>> response = controller.getSpaces().stream()
                    .map(this::toRoomResponse)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return errorResponse(e);
        }
    }

    @PostMapping("/room")
    public ResponseEntity<?> createRoom(@RequestBody Map<String, Object> data) {
        try {
            String id = getString(data, "id", "ROOM_" + System.currentTimeMillis());
            String name = getRequired(data, "name");
            int capacity = getInt(data, "capacity", 50);
            String type = getString(data, "spaceType", "LectureHall");

            // FACTORY PATTERN: Create room via factory
            Space room = SpaceFactory.createSpace(type, id, name, capacity);
            controller.addSpace(room);
            return ResponseEntity.ok(new ApiResponse(true, "Room created successfully", toRoomResponse(room)));
        } catch (Exception e) {
            return errorResponse(e);
        }
    }

    @PutMapping("/room/{id}/status")
    public ResponseEntity<?> toggleRoomStatus(@PathVariable String id) {
        try {
            controller.toggleRoomStatus(id);
            return ResponseEntity.ok(new ApiResponse(true, "Room status toggled successfully", null));
        } catch (Exception e) {
            return errorResponse(e);
        }
    }

    // ============== TIMETABLE ENDPOINTS ==============

    @PostMapping("/timetable/generate")
    public ResponseEntity<?> generateTimetable() {
        try {
            Timetable timetable = controller.generateTimetable();
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("statistics", timetable.getStatistics());
            response.put("slots", timetable.getScheduledSlots().stream()
                    .map(this::toSlotResponse).collect(Collectors.toList()));
            response.put("conflicts", timetable.getConflictingSlots().stream()
                    .map(this::toSlotResponse).collect(Collectors.toList()));
            return ResponseEntity.ok(new ApiResponse(true, "Timetable generated successfully", response));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(new ApiResponse(false, "Error: " + e.getMessage(), null));
        }
    }

    @GetMapping("/timetable/current")
    public ResponseEntity<?> getCurrentTimetable() {
        try {
            Timetable timetable = controller.getCurrentTimetable();
            if (timetable == null) {
                return ResponseEntity.ok(new ApiResponse(false, "No timetable generated yet", null));
            }
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("statistics", timetable.getStatistics());
            response.put("slots", timetable.getScheduledSlots().stream()
                    .map(this::toSlotResponse).collect(Collectors.toList()));
            response.put("conflicts", timetable.getConflictingSlots().stream()
                    .map(this::toSlotResponse).collect(Collectors.toList()));
            return ResponseEntity.ok(new ApiResponse(true, "Timetable retrieved", response));
        } catch (Exception e) {
            return errorResponse(e);
        }
    }

    @GetMapping("/timetable/section/{sectionId}")
    public ResponseEntity<?> getTimetableForSection(@PathVariable String sectionId) {
        try {
            List<Map<String, Object>> slots = controller.getTimetableForSection(sectionId).stream()
                    .map(this::toSlotResponse)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(new ApiResponse(true, "Section timetable retrieved", slots));
        } catch (Exception e) {
            return errorResponse(e);
        }
    }

    @GetMapping("/timetable/conflicts")
    public ResponseEntity<?> getConflicts() {
        try {
            List<Map<String, Object>> conflicts = controller.getConflictingSlots().stream()
                    .map(this::toSlotResponse)
                    .collect(Collectors.toList());
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("totalConflicts", conflicts.size());
            response.put("conflictingSlots", conflicts);
            return ResponseEntity.ok(new ApiResponse(true, "Conflicts retrieved", response));
        } catch (Exception e) {
            return errorResponse(e);
        }
    }

    // ============== DASHBOARD ENDPOINTS ==============

    @GetMapping("/dashboard/stats")
    public ResponseEntity<?> getDashboardStats() {
        try {
            return ResponseEntity.ok(new ApiResponse(true, "Dashboard stats retrieved", controller.getDashboardStats()));
        } catch (Exception e) {
            return errorResponse(e);
        }
    }

    @GetMapping("/dashboard/utilization")
    public ResponseEntity<?> getResourceUtilization() {
        try {
            Map<String, Object> data = new LinkedHashMap<>();
            double avgFacultyWorkload = controller.getFaculties().stream()
                    .mapToDouble(Faculty::getWorkloadPercentage).average().orElse(0.0);
            double avgRoomUtilization = controller.getSpaces().stream()
                    .mapToDouble(Space::getUtilizationPercentage).average().orElse(0.0);
            long activeRooms = controller.getSpaces().stream()
                    .filter(Space::isAvailable).count();

            data.put("averageFacultyWorkload", String.format("%.2f%%", avgFacultyWorkload));
            data.put("averageRoomUtilization", String.format("%.2f%%", avgRoomUtilization));
            data.put("activeRoomsCount", activeRooms);
            return ResponseEntity.ok(new ApiResponse(true, "Utilization retrieved", data));
        } catch (Exception e) {
            return errorResponse(e);
        }
    }

    // ============== RESPONSE BUILDERS ==============

    private Map<String, Object> toFacultyResponse(Faculty f) {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("id", f.getId()); r.put("name", f.getName()); r.put("email", f.getEmail());
        r.put("department", f.getDepartment()); r.put("maxHoursPerWeek", f.getMaxHoursPerWeek());
        r.put("assignedHoursThisWeek", f.getAssignedHoursThisWeek());
        r.put("workloadPercentage", f.getWorkloadPercentage());
        r.put("workloadStatus", f.getWorkloadStatus().toString());
        r.put("expertiseAreas", new ArrayList<>(f.getExpertiseAreas()));
        return r;
    }

    private Map<String, Object> toSubjectResponse(Subject s) {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("id", s.getId()); r.put("name", s.getName()); r.put("department", s.getDepartment());
        r.put("lectureHours", s.getLectureHours()); r.put("tutorialHours", s.getTutorialHours());
        r.put("practicalHours", s.getPracticalHours()); r.put("creditWeightage", s.getCreditWeightage());
        r.put("totalHours", s.getTotalHours());
        return r;
    }

    private Map<String, Object> toSectionResponse(Section s) {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("id", s.getId()); r.put("sectionName", s.getSectionName()); r.put("branch", s.getBranch());
        r.put("studentStrength", s.getStudentStrength()); r.put("semesterYear", s.getSemesterYear());
        r.put("assignedSubjects", new ArrayList<>(s.getAssignedSubjects()));
        return r;
    }

    private Map<String, Object> toRoomResponse(Space room) {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("id", room.getId()); r.put("name", room.getName()); r.put("capacity", room.getCapacity());
        r.put("status", room.getStatus().toString()); r.put("spaceType", room.getSpaceType());
        return r;
    }

    private Map<String, Object> toSlotResponse(ScheduleSlot slot) {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("id", slot.getId());
        r.put("activityType", slot.getActivityType() != null ? slot.getActivityType().toString() : "N/A");
        r.put("conflictStatus", slot.getConflictStatus().toString());

        r.put("section", slot.getSection() != null ?
                Map.of("id", slot.getSection().getId(), "sectionName", slot.getSection().getSectionName()) :
                Map.of("id", "N/A", "sectionName", "N/A"));
        r.put("subject", slot.getSubject() != null ?
                Map.of("id", slot.getSubject().getId(), "name", slot.getSubject().getName()) :
                Map.of("id", "N/A", "name", "N/A"));
        r.put("faculty", slot.getFaculty() != null ?
                Map.of("id", slot.getFaculty().getId(), "name", slot.getFaculty().getName()) :
                Map.of("id", "N/A", "name", "N/A"));
        r.put("room", slot.getRoom() != null ?
                Map.of("id", slot.getRoom().getId(), "name", slot.getRoom().getName()) :
                Map.of("id", "N/A", "name", "N/A"));
        r.put("timeSlot", slot.getTimeSlot() != null ?
                Map.of("day", slot.getTimeSlot().getDay().toString(),
                        "startTime", slot.getTimeSlot().getStartTime().toString(),
                        "endTime", slot.getTimeSlot().getEndTime().toString(),
                        "label", slot.getTimeSlot().toString()) :
                Map.of("day", "N/A", "startTime", "N/A", "endTime", "N/A", "label", "N/A"));
        return r;
    }

    // ============== HELPER METHODS ==============

    private ResponseEntity<ApiResponse> errorResponse(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse(false, "Error: " + e.getMessage(), null));
    }

    private String getRequired(Map<String, Object> data, String key) {
        String val = getString(data, key, null);
        if (val == null || val.isBlank()) throw new IllegalArgumentException(key + " is required");
        return val;
    }

    private String getString(Map<String, Object> data, String key, String defaultVal) {
        Object v = data.get(key);
        return v != null ? Objects.toString(v, defaultVal) : defaultVal;
    }

    private int getInt(Map<String, Object> data, String key, int defaultVal) {
        Object v = data.get(key);
        if (v instanceof Number) return ((Number) v).intValue();
        if (v instanceof String && !((String) v).isBlank()) return Integer.parseInt(((String) v).trim());
        return defaultVal;
    }

    private List<String> parseList(Object raw) {
        List<String> values = new ArrayList<>();
        if (raw == null) return values;
        if (raw instanceof Collection<?>) {
            for (Object item : (Collection<?>) raw) {
                String val = Objects.toString(item, "").trim();
                if (!val.isEmpty()) values.add(val);
            }
        } else {
            for (String part : Objects.toString(raw, "").split(",")) {
                String trimmed = part.trim();
                if (!trimmed.isEmpty()) values.add(trimmed);
            }
        }
        return values;
    }

    /** Response wrapper */
    public static class ApiResponse {
        public boolean success;
        public String message;
        public Object data;
        public ApiResponse(boolean success, String message, Object data) {
            this.success = success; this.message = message; this.data = data;
        }
    }
}
