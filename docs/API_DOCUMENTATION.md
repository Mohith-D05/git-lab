# UniPlanner API Documentation

> Complete reference for all REST API endpoints

## BASE URL

```
http://localhost:8080/api
```

## TABLE OF CONTENTS

1. [Faculty Management](#faculty-management)
2. [Subject Management](#subject-management)
3. [Room Management](#room-management)
4. [Section Management](#section-management)
5. [Timetable Generation](#timetable-generation)
6. [Dashboard & Analytics](#dashboard--analytics)
7. [Error Handling](#error-handling)

---

## FACULTY MANAGEMENT

### GET /faculty

Returns list of all faculty members

**Response:**

```json
[
  {
    "id": "FAC001",
    "name": "Dr. Rajesh Kumar",
    "email": "rajesh@university.edu",
    "department": "CS",
    "maxHoursPerWeek": 20,
    "assignedHoursThisWeek": 9,
    "workloadStatus": "GREEN",
    "expertiseAreas": ["Data Structures", "Algorithms"],
    "remainingCapacity": 11
  },
  {
    "id": "FAC002",
    "name": "Prof. Suresh Sharma",
    "email": "suresh@university.edu",
    "department": "CS",
    "maxHoursPerWeek": 20,
    "assignedHoursThisWeek": 14,
    "workloadStatus": "AMBER",
    "expertiseAreas": ["Database", "SQL"],
    "remainingCapacity": 6
  }
]
```

---

### POST /faculty

Create a new faculty member

**Request Body:**

```json
{
  "id": "FAC003",
  "name": "Dr. Neha Singh",
  "email": "neha@university.edu",
  "department": "ME",
  "maxHoursPerWeek": 22
}
```

**Response:**

```json
{
  "success": true,
  "message": "Faculty created successfully",
  "data": {
    "id": "FAC003",
    "name": "Dr. Neha Singh",
    "email": "neha@university.edu",
    "department": "ME",
    "maxHoursPerWeek": 22,
    "assignedHoursThisWeek": 0,
    "workloadStatus": "GREEN"
  }
}
```

---

### GET /faculty/{id}

Get specific faculty details

**Response:**

```json
{
  "success": true,
  "message": "Faculty retrieved",
  "data": {
    "id": "FAC001",
    "name": "Dr. Rajesh Kumar",
    "email": "rajesh@university.edu",
    "department": "CS",
    "maxHoursPerWeek": 20,
    "assignedHoursThisWeek": 9,
    "workloadStatus": "GREEN",
    "expertiseAreas": ["Data Structures", "Algorithms"],
    "associatedLabAssistants": ["LA001", "LA002"]
  }
}
```

---

### GET /faculty/{id}/workload

Get faculty workload statistics

**Response:**

```json
{
  "success": true,
  "message": "Workload retrieved",
  "data": {
    "facultyId": "FAC001",
    "name": "Dr. Rajesh Kumar",
    "assignedHours": 9,
    "maxHours": 20,
    "workloadPercentage": 45.0,
    "workloadStatus": "GREEN",
    "remainingCapacity": 11
  }
}
```

| Status | Percentage | Meaning                               |
| ------ | ---------- | ------------------------------------- |
| GREEN  | 0-50%      | Available for more assignments        |
| AMBER  | 50-80%     | Moderate workload, careful scheduling |
| RED    | 80%+       | At capacity, cannot assign more hours |

---

### PUT /faculty/{id}

Update faculty information

**Request Body:**

```json
{
  "name": "Dr. Rajesh Kumar (Updated)",
  "maxHoursPerWeek": 22,
  "expertiseAreas": ["Data Structures", "Algorithms", "Big Data"]
}
```

---

### DELETE /faculty/{id}

Delete a faculty member

**Response:**

```json
{
  "success": true,
  "message": "Faculty deleted successfully"
}
```

---

## SUBJECT MANAGEMENT

### GET /subject

Get all subjects

**Response:**

```json
[
  {
    "id": "SUB001",
    "name": "Data Structures",
    "department": "CS",
    "lectureHours": 3,
    "tutorialHours": 1,
    "practicalHours": 2,
    "creditWeightage": 4,
    "totalHours": 6,
    "requiresLab": true
  },
  {
    "id": "SUB002",
    "name": "Database Management",
    "department": "CS",
    "lectureHours": 3,
    "tutorialHours": 1,
    "practicalHours": 2,
    "creditWeightage": 4,
    "totalHours": 6,
    "requiresLab": true
  }
]
```

---

### POST /subject

Create new subject

**Request Body:**

```json
{
  "id": "SUB003",
  "name": "Artificial Intelligence",
  "department": "CS",
  "lectureHours": 3,
  "tutorialHours": 1,
  "practicalHours": 1,
  "creditWeightage": 4
}
```

---

### DELETE /subject/{id}

Delete a subject

---

## ROOM MANAGEMENT

### GET /room

Get all rooms

**Response:**

```json
[
  {
    "id": "ROOM001",
    "name": "LH-101",
    "capacity": 100,
    "spaceType": "LectureHall",
    "status": "ACTIVE",
    "features": {
      "hasProjector": true,
      "hasWhiteboard": true,
      "hasAirConditioning": false
    },
    "utilizationPercentage": 60.5
  },
  {
    "id": "ROOM002",
    "name": "Lab-201",
    "capacity": 50,
    "spaceType": "ComputerLab",
    "status": "ACTIVE",
    "features": {
      "numberOfSystems": 50,
      "operatingSystem": "Windows",
      "hasSufficientSystems": true
    },
    "utilizationPercentage": 45.0
  }
]
```

---

### POST /room

Create new room

**Request Body (Lecture Hall):**

```json
{
  "id": "ROOM003",
  "name": "LH-102",
  "capacity": 80,
  "spaceType": "LectureHall",
  "hasProjector": true,
  "hasWhiteboard": true,
  "hasAirConditioning": true
}
```

**Request Body (Computer Lab):**

```json
{
  "id": "ROOM004",
  "name": "Lab-301",
  "capacity": 40,
  "spaceType": "ComputerLab",
  "numberOfSystems": 40,
  "operatingSystem": "Linux",
  "softwareAvailable": "Java, Python, C++",
  "hasProjector": true
}
```

---

### PUT /room/{id}/status

Toggle room status (Active ↔ Maintenance)

**Response:**

```json
{
  "success": true,
  "message": "Room status toggled successfully",
  "data": {
    "roomId": "ROOM001",
    "newStatus": "MAINTENANCE"
  }
}
```

---

## SECTION MANAGEMENT

### GET /section

Get all sections

**Response:**

```json
[
  {
    "id": "SEC001",
    "sectionName": "CS-A",
    "branch": "Computer Science",
    "studentStrength": 60,
    "semesterYear": 3,
    "assignedSubjects": ["SUB001", "SUB002", "SUB003"]
  },
  {
    "id": "SEC002",
    "sectionName": "CS-B",
    "branch": "Computer Science",
    "studentStrength": 55,
    "semesterYear": 3,
    "assignedSubjects": ["SUB001", "SUB002", "SUB003"]
  }
]
```

---

### POST /section

Create new section

**Request Body:**

```json
{
  "id": "SEC003",
  "sectionName": "EC-A",
  "branch": "Electronics & Communication",
  "studentStrength": 50,
  "semesterYear": 3
}
```

---

## TIMETABLE GENERATION

### POST /timetable/generate

Generate timetable using CSP algorithm

**Response:**

```json
{
  "success": true,
  "message": "Timetable generated successfully",
  "data": {
    "totalSlots": 48,
    "conflictingSlots": 2,
    "conflictPercentage": 4.17,
    "isValid": false,
    "departmentName": "Multi-Department",
    "semester": "2024-2025"
  }
}
```

**Status Codes:**
| Value | Meaning |
|-------|---------|
| `isValid: true` | No conflicts, ready to use |
| `isValid: false` | Has conflicts, needs manual resolution |

---

### GET /timetable/current

Get currently generated timetable

**Response:**

```json
{
  "success": true,
  "message": "Current timetable retrieved",
  "data": {
    "id": "TT_UUID",
    "totalSlots": 48,
    "conflictingSlots": 2,
    "conflictPercentage": 4.17,
    "isValid": false,
    "departmentName": "Multi-Department",
    "semester": "2024-2025",
    "generatedAt": "2024-04-15T10:30:00Z"
  }
}
```

---

### GET /timetable/section/{id}

Get timetable for specific section

**Response:**

```json
{
  "success": true,
  "message": "Timetable retrieved",
  "data": [
    {
      "id": "SLOT_001",
      "section": "CS-A",
      "subject": "Data Structures",
      "faculty": "Dr. Rajesh Kumar",
      "room": "LH-101",
      "timeSlot": "MONDAY 9:00-10:00",
      "activityType": "LECTURE",
      "conflictStatus": "VALID"
    },
    {
      "id": "SLOT_002",
      "section": "CS-A",
      "subject": "Data Structures",
      "faculty": "Dr. Rajesh Kumar",
      "room": "Lab-201",
      "timeSlot": "TUESDAY 10:00-11:00",
      "activityType": "PRACTICAL",
      "conflictStatus": "VALID"
    }
  ]
}
```

---

### GET /timetable/conflicts

Get all conflicting slots

**Response:**

```json
{
  "success": true,
  "message": "Conflicts retrieved",
  "data": {
    "totalConflicts": 2,
    "conflictingSlots": [
      {
        "id": "CONFLICT_001",
        "section": "CS-A",
        "subject": "Data Structures",
        "faculty": "Dr. Kumar",
        "room": "LH-101",
        "timeSlot": "MONDAY 11:00-12:00",
        "conflictStatus": "FACULTY_DOUBLE_BOOKING",
        "conflictReason": "Already assigned at 10:00-11:00"
      },
      {
        "id": "CONFLICT_002",
        "section": "CS-B",
        "subject": "Database",
        "faculty": "Prof. Sharma",
        "room": "Lab-201",
        "timeSlot": "MONDAY 11:00-12:00",
        "conflictStatus": "ROOM_OVERLAP",
        "conflictReason": "Room already occupied"
      }
    ]
  }
}
```

**Conflict Types:**
| Type | Meaning | Severity |
|------|---------|----------|
| `VALID` | No conflicts | ✅ OK |
| `FACULTY_DOUBLE_BOOKING` | Faculty assigned at same time | ⚠️ Hard Constraint Violated |
| `ROOM_OVERLAP` | Room already occupied | ⚠️ Hard Constraint Violated |
| `BOTH_CONFLICT` | Both faculty and room conflict | ⚠️⚠️ Critical |

---

### POST /timetable/resolve/{slotId}

Manually resolve a conflicting slot

**Request Body:**

```json
{
  "facultyId": "FAC002",
  "roomId": "ROOM003"
}
```

**Response:**

```json
{
  "success": true,
  "message": "Conflict resolved successfully",
  "data": {
    "slotId": "CONFLICT_001",
    "newFacultyId": "FAC002",
    "newRoomId": "ROOM003"
  }
}
```

---

### GET /timetable/export/pdf

Export timetable as PDF

**Query Parameters:**

- `sectionId` (optional): Export specific section

**Response:** Binary PDF file

---

### GET /timetable/export/excel

Export timetable as Excel

**Query Parameters:**

- `sectionId` (optional): Export specific section

**Response:** Binary XLSX file

---

## DASHBOARD & ANALYTICS

### GET /dashboard/stats

Get dashboard statistics

**Response:**

```json
{
  "success": true,
  "message": "Dashboard stats retrieved",
  "data": {
    "availableFacultyCount": 8,
    "totalFacultyCount": 15,
    "averageRoomUtilization": "52.3%",
    "conflictCount": 2,
    "timetableGenerated": true,
    "totalSections": 6,
    "totalSubjects": 18,
    "lastGeneratedAt": "2024-04-15T10:30:00Z"
  }
}
```

---

### GET /dashboard/utilization

Get resource utilization metrics

**Response:**

```json
{
  "success": true,
  "message": "Utilization retrieved",
  "data": {
    "averageFacultyWorkload": "65.3%",
    "averageRoomUtilization": "52.3%",
    "activeRoomsCount": 8,
    "maintenanceRoomsCount": 2,
    "overhiredFaculty": 2,
    "underutilizedSlots": 5
  }
}
```

---

## ERROR HANDLING

### HTTP Status Codes

| Code | Meaning                                 |
| ---- | --------------------------------------- |
| 200  | OK - Request successful                 |
| 201  | Created - Resource created successfully |
| 400  | Bad Request - Invalid input             |
| 401  | Unauthorized - Authentication required  |
| 404  | Not Found - Resource doesn't exist      |
| 409  | Conflict - Resource state conflict      |
| 500  | Internal Server Error                   |

### Standard Error Response

```json
{
  "success": false,
  "message": "Faculty max hours must be between 1 and 50",
  "errorCode": "VALIDATION_ERROR",
  "timestamp": "2024-04-15T10:30:00Z"
}
```

### Common Errors

**1. Faculty Capacity Exceeded**

```json
{
  "success": false,
  "message": "Cannot assign 5 more hours to Dr. Kumar (would exceed 20 max)",
  "errorCode": "CAPACITY_EXCEEDED"
}
```

**2. No Schedule Generated**

```json
{
  "success": false,
  "message": "No timetable has been generated yet",
  "errorCode": "NO_TIMETABLE",
  "suggestion": "Call POST /api/timetable/generate first"
}
```

**3. Invalid Room Type**

```json
{
  "success": false,
  "message": "Room LH-101 (LectureHall) cannot accommodate PRACTICAL activity",
  "errorCode": "INVALID_ROOM_TYPE"
}
```

---

## QUICK START EXAMPLES

### Create Faculty with Expertise

```bash
curl -X POST http://localhost:8080/api/faculty \
  -H "Content-Type: application/json" \
  -d '{
    "id": "FAC004",
    "name": "Dr. Amit Patel",
    "email": "amit@university.edu",
    "department": "CS",
    "maxHoursPerWeek": 20,
    "expertiseAreas": ["Machine Learning", "AI"]
  }'
```

### Generate Timetable

```bash
curl -X POST http://localhost:8080/api/timetable/generate
```

### Get Section Schedule

```bash
curl http://localhost:8080/api/timetable/section/SEC001
```

### Export to Excel

```bash
curl http://localhost:8080/api/timetable/export/excel?sectionId=SEC001 \
  -o timetable_CS-A.xlsx
```

---

**For more details**, see [README.md](../README.md)
