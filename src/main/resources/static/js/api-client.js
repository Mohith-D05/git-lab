/* ============================================
   UniPlanner API Client
   Handles all communication with the backend
   ============================================ */

const API_BASE_URL = '/api';

/**
 * Generic fetch wrapper with error handling
 */
async function apiCall(endpoint, method = 'GET', data = null) {
    const options = {
        method,
        headers: {
            'Content-Type': 'application/json',
        }
    };

    if (data) {
        options.body = JSON.stringify(data);
    }

    try {
        console.log(`API ${method} ${endpoint}:`, data);
        const response = await fetch(`${API_BASE_URL}${endpoint}`, options);
        
        const responseText = await response.text();
        console.log(`API Response (${endpoint}):`, responseText);
        
        if (!response.ok) {
            console.error(`HTTP ${response.status}: ${response.statusText}`);
            showNotification(`Error: HTTP ${response.status}`, 'error');
            return null;
        }

        const jsonResponse = JSON.parse(responseText);
        console.log(`API Parsed Response (${endpoint}):`, jsonResponse);
        return jsonResponse;
    } catch (error) {
        console.error('API Error:', error);
        showNotification(`API Error: ${error.message}`, 'error');
        return null;
    }
}

/* ============================================
   FACULTY API CALLS
   ============================================ */

async function getAllFaculties() {
    return apiCall('/faculty');
}

async function createFaculty(facultyData) {
    return apiCall('/faculty', 'POST', facultyData);
}

async function updateFaculty(facultyId, facultyData) {
    return apiCall(`/faculty/${facultyId}`, 'PUT', facultyData);
}

async function deleteFaculty(facultyId) {
    return apiCall(`/faculty/${facultyId}`, 'DELETE');
}

async function getFacultyWorkload(facultyId) {
    return apiCall(`/faculty/${facultyId}/workload`);
}

/* ============================================
   SUBJECT API CALLS
   ============================================ */

async function getAllSubjects() {
    return apiCall('/subject');
}

async function createSubject(subjectData) {
    return apiCall('/subject', 'POST', subjectData);
}

async function deleteSubject(subjectId) {
    return apiCall(`/subject/${subjectId}`, 'DELETE');
}

/* ============================================
   SECTION API CALLS
   ============================================ */

async function getAllSections() {
    return apiCall('/section');
}

async function createSection(sectionData) {
    return apiCall('/section', 'POST', sectionData);
}

async function deleteSection(sectionId) {
    return apiCall(`/section/${sectionId}`, 'DELETE');
}

/* ============================================
   ROOM API CALLS
   ============================================ */

async function getAllRooms() {
    return apiCall('/room');
}

async function createRoom(roomData) {
    return apiCall('/room', 'POST', roomData);
}

async function toggleRoomStatus(roomId) {
    return apiCall(`/room/${roomId}/status`, 'PUT');
}

/* ============================================
   TIMETABLE API CALLS
   ============================================ */

async function generateTimetable() {
    return apiCall('/timetable/generate', 'POST');
}

async function getCurrentTimetable() {
    return apiCall('/timetable/current');
}

async function getTimetableForSection(sectionId) {
    return apiCall(`/timetable/section/${sectionId}`);
}

async function getConflicts() {
    return apiCall('/timetable/conflicts');
}

async function resolveConflict(slotId, resolutionData) {
    return apiCall(`/timetable/resolve/${slotId}`, 'POST', resolutionData);
}

/* ============================================
   DASHBOARD API CALLS
   ============================================ */

async function getDashboardStats() {
    return apiCall('/dashboard/stats');
}

async function getResourceUtilization() {
    return apiCall('/dashboard/utilization');
}

/* ============================================
   EXPORT API CALLS
   ============================================ */

async function exportToExcel(sectionId = null) {
    const endpoint = sectionId ? 
        `/timetable/export/excel/${sectionId}` : 
        '/timetable/export/excel';
    
    const response = await fetch(`${API_BASE_URL}${endpoint}`);
    if (response.ok) {
        const blob = await response.blob();
        downloadFile(blob, `timetable_${new Date().getTime()}.xlsx`);
    }
}

async function exportToPDF(sectionId = null) {
    const endpoint = sectionId ? 
        `/timetable/export/pdf/${sectionId}` : 
        '/timetable/export/pdf';
    
    const response = await fetch(`${API_BASE_URL}${endpoint}`);
    if (response.ok) {
        const blob = await response.blob();
        downloadFile(blob, `timetable_${new Date().getTime()}.pdf`);
    }
}

/* ============================================
   UTILITY FUNCTIONS
   ============================================ */

/**
 * Download file from blob
 */
function downloadFile(blob, filename) {
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    window.URL.revokeObjectURL(url);
    document.body.removeChild(a);
}

/**
 * Show notification
 */
function showNotification(message, type = 'info') {
    console.log(`[${type.toUpperCase()}] ${message}`);
    
    // Create notification element
    const notification = document.createElement('div');
    notification.className = `notification notification-${type}`;
    notification.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        padding: 15px 20px;
        background: ${type === 'success' ? '#4CAF50' : type === 'error' ? '#f44336' : '#2196F3'};
        color: white;
        border-radius: 4px;
        box-shadow: 0 2px 5px rgba(0,0,0,0.2);
        z-index: 10000;
        font-weight: bold;
        max-width: 400px;
    `;
    notification.textContent = message;
    document.body.appendChild(notification);
    
    // Auto-remove after 3 seconds
    setTimeout(() => {
        notification.remove();
    }, 3000);
}

/**
 * Show success message
 */
function showSuccess(message) {
    showNotification(message, 'success');
}

/**
 * Show error message
 */
function showError(message) {
    showNotification(message, 'error');
}
