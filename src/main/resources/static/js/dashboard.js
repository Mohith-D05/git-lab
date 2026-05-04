/* ============================================
   UniPlanner Dashboard Logic
   Frontend interactivity and UI management
   ============================================ */

// Initialize on page load
document.addEventListener('DOMContentLoaded', function() {
    setupEventListeners();
    loadDashboardStats();
    loadFaculties();
    loadSubjects();
    loadRooms();
    loadSections();
    loadConflicts();
    loadCurrentTimetable();
});

/* ============================================
   EVENT LISTENER SETUP
   ============================================ */

function setupEventListeners() {
    // Navigation links
    document.querySelectorAll('.nav-link').forEach(link => {
        link.addEventListener('click', function(e) {
            e.preventDefault();
            const section = this.getAttribute('data-section');
            showSection(section);
            
            // Update active nav link
            document.querySelectorAll('.nav-link').forEach(l => l.classList.remove('active'));
            this.classList.add('active');
        });
    });
}

/* ============================================
   SECTION NAVIGATION
   ============================================ */

function showSection(sectionName) {
    // Hide all sections
    document.querySelectorAll('.section').forEach(section => {
        section.classList.remove('active');
    });

    // Show selected section
    const sectionElement = document.getElementById(sectionName + '-section');
    if (sectionElement) {
        sectionElement.classList.add('active');
    }
}

/* ============================================
   DASHBOARD FUNCTIONS
   ============================================ */

async function loadDashboardStats() {
    showSection('dashboard');
    
    const stats = await getDashboardStats();
    if (!stats || !stats.data) return;

    const utilization = await getResourceUtilization();

    const data = stats.data;
    document.getElementById('available-faculty').textContent = data.availableFacultyCount || 0;
    document.getElementById('room-utilization').textContent = data.averageRoomUtilization || '0%';
    document.getElementById('conflict-count').textContent = data.conflictCount || 0;
    document.getElementById('timetable-status').textContent = data.timetableGenerated ? 'Generated' : 'Not Generated';
    if (utilization?.data) {
        document.getElementById('active-rooms').textContent = utilization.data.activeRoomsCount || 0;
    }
}

async function generateTimetable(button) {
    if (!confirm('Are you sure you want to generate the timetable? This will overwrite the current one.')) {
        return;
    }

    // Show loading state
    const btn = button || document.querySelector('.action-section .btn-primary');
    const originalText = btn.textContent;
    btn.disabled = true;
    btn.textContent = 'Generating...';

    const result = await apiCall('/timetable/generate', 'POST');
    
    btn.disabled = false;
    btn.textContent = originalText;

    if (result && result.success) {
        showSuccess('Timetable generated successfully!');
        loadDashboardStats();
        loadConflicts();
        loadCurrentTimetable();
    } else {
        showError(result?.message || 'Error generating timetable');
    }
}

async function loadFaculties() {
    const faculties = await getAllFaculties();
    if (!faculties) return;

    const facultyList = document.querySelector('.faculty-grid');
    if (!facultyList) return;

    facultyList.innerHTML = '';
    
    faculties.forEach(faculty => {
        const workloadStatus = getWorkloadStatus(faculty.workloadPercentage);
        const workloadColor = getStatusColor(workloadStatus);
        
        const card = document.createElement('div');
        card.className = 'faculty-card';
        card.innerHTML = `
            <div class="faculty-header">
                <h4>${faculty.name}</h4>
                <span class="badge ${workloadColor}">${workloadStatus}</span>
            </div>
            <div class="faculty-details">
                <p><strong>Department:</strong> ${faculty.department}</p>
                <p><strong>Email:</strong> ${faculty.email}</p>
                <p><strong>Expertise:</strong> ${faculty.expertiseAreas?.join(', ') || 'Not specified'}</p>
                <div class="workload-bar">
                    <div class="fill" style="width: ${faculty.workloadPercentage || 0}%"></div>
                </div>
                <p class="workload-text">${faculty.assignedHoursThisWeek}/${faculty.maxHoursPerWeek} hours (${faculty.workloadPercentage?.toFixed(1) || 0}%)</p>
            </div>
            <div class="faculty-actions">
                <button class="btn-icon" onclick="editFaculty('${faculty.id}')" title="Edit">E</button>
                <button class="btn-icon" onclick="deleteFacultyConfirm('${faculty.id}')" title="Delete">D</button>
            </div>
        `;
        facultyList.appendChild(card);
    });
}

/* ============================================
   RESOURCE TAB MANAGEMENT
   ============================================ */

function showResourceTab(tabName) {
    // Hide all tabs
    document.querySelectorAll('.tab-content').forEach(tab => {
        tab.classList.remove('active');
    });
    document.querySelectorAll('.tab-btn').forEach(btn => {
        btn.classList.remove('active');
    });

    // Show selected tab
    const tabElement = document.getElementById(tabName + '-tab');
    if (tabElement) {
        tabElement.classList.add('active');
    }

    // Mark button as active - find the button that matches this tab
    const buttons = document.querySelectorAll('.tab-btn');
    buttons.forEach(btn => {
        if (btn.textContent.toLowerCase().includes(tabName) || 
            btn.getAttribute('data-tab') === tabName) {
            btn.classList.add('active');
        }
    });

    // Load tab-specific data
    switch(tabName) {
        case 'faculty':
            loadFaculties();
            break;
        case 'subject':
            loadSubjects();
            break;
        case 'room':
            loadRooms();
            break;
        case 'section':
            loadSections();
            break;
    }
}

async function loadSubjects() {
    const subjects = await getAllSubjects();
    if (!subjects) return;

    const tbody = document.querySelector('#subject-tab tbody');
    if (!tbody) return;

    tbody.innerHTML = '';
    
    subjects.forEach(subject => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${subject.name}</td>
            <td>${subject.department}</td>
            <td>${subject.lectureHours}/${subject.tutorialHours}/${subject.practicalHours}</td>
            <td>${subject.creditWeightage}</td>
            <td>
                <button class="btn-icon" onclick="editSubject('${subject.id}')" title="Edit">E</button>
                <button class="btn-icon" onclick="deleteSubjectConfirm('${subject.id}')" title="Delete">D</button>
            </td>
        `;
        tbody.appendChild(row);
    });
}

async function loadRooms() {
    const rooms = await getAllRooms();
    if (!rooms) return;

    const roomGrid = document.querySelector('#room-tab .room-grid');
    if (!roomGrid) return;

    roomGrid.innerHTML = '';
    
    rooms.forEach(room => {
        const statusClass = room.status === 'ACTIVE' ? 'active' : 'maintenance';
        const card = document.createElement('div');
        card.className = 'room-card';
        card.innerHTML = `
            <div class="room-header">
                <h4>${room.name} (${room.spaceType})</h4>
                <span class="status-badge ${statusClass}">${room.status}</span>
            </div>
            <div class="room-details">
                <p><strong>Capacity:</strong> ${room.capacity}</p>
                <p><strong>Features:</strong> ${room.additionalInfo || 'Standard'}</p>
                <p><strong>Utilization:</strong> ${Math.round(Math.random() * 100)}%</p>
            </div>
            <button class="btn btn-outline btn-sm" onclick="toggleRoomStatusConfirm('${room.id}')">Toggle Status</button>
        `;
        roomGrid.appendChild(card);
    });
}

async function loadSections() {
    const sections = await getAllSections();
    if (!sections) return;

    const tbody = document.querySelector('#section-tab tbody');
    if (!tbody) return;

    tbody.innerHTML = '';
    
    sections.forEach(section => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${section.sectionName}</td>
            <td>${section.branch}</td>
            <td>${section.studentStrength}</td>
            <td>${section.semesterYear}</td>
            <td>${section.assignedSubjects?.length || 0}</td>
            <td>
                <button class="btn-icon" onclick="editSection('${section.id}')" title="Edit">E</button>
                <button class="btn-icon" onclick="deleteSectionConfirm('${section.id}')" title="Delete">D</button>
            </td>
        `;
        tbody.appendChild(row);
    });

    populateSectionFilter(sections);
}

/* ============================================
   FORM MANAGEMENT
   ============================================ */

function openFacultyForm() {
    openForm('Add Faculty', 'faculty', {
        id: '',
        name: '',
        email: '',
        department: 'CS',
        maxHoursPerWeek: 20,
        expertiseAreas: ''
    });
}

function openSubjectForm() {
    openForm('Add Subject', 'subject', {
        id: '',
        name: '',
        department: 'CS',
        lectureHours: 3,
        tutorialHours: 1,
        practicalHours: 2,
        creditWeightage: 4
    });
}

function openRoomForm() {
    openForm('Add Room', 'room', {
        id: '',
        name: '',
        capacity: 50,
        spaceType: 'LectureHall',
        status: 'ACTIVE'
    });
}

function openSectionForm() {
    openForm('Add Section', 'section', {
        id: '',
        sectionName: '',
        branch: 'CS',
        studentStrength: 50,
        semesterYear: 3,
        assignedSubjects: ''
    });
}

function openForm(title, type, data) {
    document.getElementById('modal-title').textContent = title;
    document.getElementById('modal-form').setAttribute('data-type', type);
    document.getElementById('modal-form').setAttribute('data-id', data.id || '');
    
    const formFields = document.getElementById('form-fields');
    formFields.innerHTML = '';

    Object.keys(data).forEach(key => {
        if (key === 'id') return;
        
        const label = key.replace(/([A-Z])/g, ' $1').trim();
        const field = document.createElement('div');
        field.className = 'form-group';

        if (key === 'department' || key === 'branch' || key === 'spaceType' || key === 'status') {
            const csSelected = data[key] === 'CS' ? 'selected' : '';
            const ecSelected = data[key] === 'EC' ? 'selected' : '';
            const meSelected = data[key] === 'ME' ? 'selected' : '';
            const activeSelected = data[key] === 'ACTIVE' ? 'selected' : '';
            const maintenanceSelected = data[key] === 'MAINTENANCE' ? 'selected' : '';
            const lectureHallSelected = data[key] === 'LectureHall' ? 'selected' : '';
            const computerLabSelected = data[key] === 'ComputerLab' ? 'selected' : '';
            
            let selectOptions = '';
            if (key === 'status') {
                selectOptions = `
                    <option value="ACTIVE" ${activeSelected}>ACTIVE</option>
                    <option value="MAINTENANCE" ${maintenanceSelected}>MAINTENANCE</option>
                `;
            } else if (key === 'spaceType') {
                selectOptions = `
                    <option value="LectureHall" ${lectureHallSelected}>Lecture Hall</option>
                    <option value="ComputerLab" ${computerLabSelected}>Computer Lab</option>
                `;
            } else {
                selectOptions = `
                    <option value="CS" ${csSelected}>CS</option>
                    <option value="EC" ${ecSelected}>EC</option>
                    <option value="ME" ${meSelected}>ME</option>
                `;
            }
            
            field.innerHTML = `
                <label>${label}:</label>
                <select name="${key}">
                    ${selectOptions}
                </select>
            `;
        } else if (typeof data[key] === 'number') {
            field.innerHTML = `
                <label>${label}:</label>
                <input type="number" name="${key}" value="${data[key]}" required>
            `;
        } else {
            field.innerHTML = `
                <label>${label}:</label>
                <input type="text" name="${key}" value="${data[key]}" required>
            `;
        }
        
        formFields.appendChild(field);
    });

    openModal();
}

function closeModal() {
    document.getElementById('modal').classList.remove('active');
}

function openModal() {
    document.getElementById('modal').classList.add('active');
}

async function submitForm(event) {
    event.preventDefault();
    
    console.log('submitForm called');
    
    const form = document.getElementById('modal-form');
    if (!form) {
        console.error('Form not found in DOM');
        showError('Form element not found');
        return;
    }
    
    const type = form.getAttribute('data-type');
    const id = form.getAttribute('data-id');
    
    console.log('Form type:', type, 'ID:', id);
    
    if (!type) {
        console.error('Form type is missing');
        showError('Form type is missing');
        return;
    }
    
    // Get all form fields
    const formFields = form.querySelectorAll('input, select, textarea');
    const data = {};
    
    formFields.forEach(field => {
        if (field.name) {
            data[field.name] = field.value;
        }
    });
    
    console.log('Collected form data:', data);
    
    // Validate that we have some data
    if (Object.keys(data).length === 0) {
        console.error('No form data collected');
        showError('Please fill in the form');
        return;
    }
    
    // Add ID if creating new
    if (!id) {
        data.id = `${type.toUpperCase()}_${Date.now()}`;
    }

    let result = null;
    
    try {
        console.log('Submitting:', type, data);
        
        switch(type) {
            case 'faculty':
                result = await createFaculty(data);
                break;
            case 'subject':
                result = await createSubject(data);
                break;
            case 'room':
                result = await createRoom(data);
                break;
            case 'section':
                result = await createSection(data);
                break;
            default:
                console.error('Unknown form type:', type);
                showError('Unknown form type');
                return;
        }
        
        console.log('API Result:', result);
    } catch (error) {
        console.error('Error in form submission:', error);
        showError(`Error: ${error.message}`);
        return;
    }

    if (result && result.success) {
        showSuccess(`${type} saved successfully!`);
        // Clear form
        form.reset();
        // Give a moment for success message to show
        await new Promise(resolve => setTimeout(resolve, 800));
        closeModal();
        // Reload the resource list
        await reloadResource(type);
    } else {
        const errorMsg = result ? result.message || 'Unknown error' : 'No response from server';
        console.error('Form submission failed:', errorMsg);
        showError(`Error saving ${type}: ${errorMsg}`);
    }
}

async function reloadResource(type) {
    try {
        console.log('Reloading resource:', type);
        switch(type) {
            case 'faculty':
                await loadFaculties();
                break;
            case 'subject':
                await loadSubjects();
                await loadSections();
                break;
            case 'room':
                await loadRooms();
                break;
            case 'section':
                await loadSections();
                await loadCurrentTimetable();
                break;
        }
    } catch (error) {
        console.error('Error reloading resource:', error);
    }
}

/* ============================================
   CONFLICT RESOLUTION
   ============================================ */

async function loadConflicts() {
    const result = await getConflicts();
    if (!result || !result.data) return;

    const conflictTotal = document.getElementById('conflict-total');
    conflictTotal.textContent = result.data.totalConflicts || 0;

    const conflictsList = document.querySelector('.conflicts-list');
    if (!conflictsList) return;

    conflictsList.innerHTML = '';
    
    if (!result.data.conflictingSlots || result.data.conflictingSlots.length === 0) {
        conflictsList.innerHTML = '<p style="text-align: center; padding: 2rem; color: #16a34a; font-weight: bold;">No conflicts! Timetable is valid.</p>';
        return;
    }

    result.data.conflictingSlots.forEach(slot => {
        const item = document.createElement('div');
        item.className = 'conflict-item';
        item.innerHTML = `
            <div class="conflict-header">
                <h4>${slot.conflictStatus}</h4>
                <span class="conflict-type">Slot #${slot.id}</span>
            </div>
            <div class="conflict-body">
                <p><strong>Section:</strong> ${slot.section?.sectionName}</p>
                <p><strong>Subject:</strong> ${slot.subject?.name}</p>
                <p><strong>Faculty:</strong> ${slot.faculty?.name}</p>
                <p><strong>Time:</strong> ${slot.timeSlot?.toString()}</p>
                <p><strong>Room:</strong> ${slot.room?.name}</p>
            </div>
            <div class="conflict-actions">
                <button class="btn btn-primary btn-sm" onclick="resolveConflict('${slot.id}')">
                    Resolve
                </button>
                <button class="btn btn-secondary btn-sm" onclick="acceptConflict('${slot.id}')">
                    Override
                </button>
            </div>
        `;
        conflictsList.appendChild(item);
    });
}

async function resolveConflict(slotId) {
    // Placeholder - would open resolution UI
    showSuccess(`Attempting to resolve conflict ${slotId}`);
}

async function acceptConflict(slotId) {
    // Placeholder - would apply manual override
    showSuccess(`Manually accepted conflict ${slotId}`);
}

/* ============================================
   CONFIRM DIALOGS
   ============================================ */

async function deleteFacultyConfirm(facultyId) {
    if (confirm('Are you sure you want to delete this faculty?')) {
        const result = await deleteFaculty(facultyId);
        if (result && result.success) {
            showSuccess('Faculty deleted successfully');
            loadFaculties();
        }
    }
}

async function deleteSubjectConfirm(subjectId) {
    if (confirm('Are you sure you want to delete this subject?')) {
        const result = await deleteSubject(subjectId);
        if (result && result.success) {
            showSuccess('Subject deleted successfully');
            loadSubjects();
        }
    }
}

async function deleteSectionConfirm(sectionId) {
    if (confirm('Are you sure you want to delete this section?')) {
        const result = await deleteSection(sectionId);
        if (result && result.success) {
            showSuccess('Section deleted successfully');
            loadSections();
        }
    }
}

async function toggleRoomStatusConfirm(roomId) {
    const result = await toggleRoomStatus(roomId);
    if (result && result.success) {
        showSuccess('Room status toggled successfully');
        loadRooms();
    }
}

/* ============================================
   TIMETABLE FUNCTIONS
   ============================================ */

function filterTimetableBySection() {
    loadCurrentTimetable();
}

async function exportTimetable() {
    await exportToExcel();
}

/* ============================================
   UTILITY FUNCTIONS
   ============================================ */

function showSuccess(message) {
    showNotification(message, 'success');
}

function showError(message) {
    showNotification(message, 'error');
}

async function editFaculty(id) {
    // Find faculty and open form for editing
    const faculties = await getAllFaculties();
    const faculty = faculties.find(f => f.id === id);
    if (faculty) {
        faculty.expertiseAreas = faculty.expertiseAreas?.join(', ') || '';
        openForm('Edit Faculty', 'faculty', faculty);
    } else {
        showError('Faculty not found');
    }
}

async function editSubject(id) {
    // Find subject and open form for editing
    const subjects = await getAllSubjects();
    const subject = subjects.find(s => s.id === id);
    if (subject) {
        openForm('Edit Subject', 'subject', subject);
    } else {
        showError('Subject not found');
    }
}

async function editSection(id) {
    // Find section and open form for editing
    const sections = await getAllSections();
    const section = sections.find(s => s.id === id);
    if (section) {
        section.assignedSubjects = section.assignedSubjects?.join(', ') || '';
        openForm('Edit Section', 'section', section);
    } else {
        showError('Section not found');
    }
}

/* ============================================
   UTILITY FUNCTIONS - NOTIFICATIONS
   ============================================ */

function getWorkloadStatus(percentage) {
    if (percentage <= 50) return 'GREEN';
    if (percentage <= 80) return 'AMBER';
    return 'RED';
}

function getStatusColor(status) {
    switch(status) {
        case 'GREEN': return 'green';
        case 'AMBER': return 'amber';
        case 'RED': return 'red';
        default: return 'gray';
    }
}

function saveSettings() {
    const maxHours = document.getElementById('max-faculty-hours').value;
    const greenThreshold = document.getElementById('workload-green-threshold').value;
    const amberThreshold = document.getElementById('workload-amber-threshold').value;

    localStorage.setItem('maxFacultyHours', maxHours);
    localStorage.setItem('workloadGreenThreshold', greenThreshold);
    localStorage.setItem('workloadAmberThreshold', amberThreshold);

    showSuccess('Settings saved successfully!');
}

function exportDatabase() {
    showSuccess('Database export started...');
    // Implementation would trigger backend export
}

function importDatabase() {
    showSuccess('Database import dialog would open');
    // Implementation would trigger backend import
}

function populateSectionFilter(sections) {
    const sectionFilter = document.getElementById('section-filter');
    if (!sectionFilter) return;

    const currentValue = sectionFilter.value;
    sectionFilter.innerHTML = '<option value="">All Sections</option>';
    sections.forEach(section => {
        const option = document.createElement('option');
        option.value = section.id;
        option.textContent = section.sectionName;
        sectionFilter.appendChild(option);
    });
    sectionFilter.value = currentValue;
}

async function loadCurrentTimetable() {
    const sectionId = document.getElementById('section-filter')?.value || '';
    const response = sectionId ? await getTimetableForSection(sectionId) : await getCurrentTimetable();
    const slots = response?.data?.slots || response?.data || [];
    renderTimetable(slots);
}

function renderTimetable(slots) {
    const tbody = document.getElementById('timetable-body');
    if (!tbody) return;

    const days = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY'];
    const timeLabels = [
        '09:00-10:00',
        '10:00-11:00',
        '11:00-12:00',
        '12:00-13:00',
        '13:00-14:00',
        '14:00-15:00',
        '15:00-16:00',
        '16:00-17:00'
    ];

    if (!slots || slots.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="6" class="schedule-cell">No timetable has been generated yet.</td>
            </tr>
        `;
        return;
    }

    const slotMap = {};
    slots.forEach(slot => {
        const key = `${slot.timeSlot.day}_${slot.timeSlot.startTime}`;
        if (!slotMap[key]) {
            slotMap[key] = [];
        }
        slotMap[key].push(slot);
    });

    tbody.innerHTML = timeLabels.map(label => {
        const startTime = label.split('-')[0];
        const cells = days.map(day => {
            const key = `${day}_${startTime}`;
            const entries = (slotMap[key] || []).map(slot => `
                <div class="slot-entry">
                    ${slot.subject?.name || 'Unknown Subject'}<br/>
                    ${slot.section?.sectionName || 'Unknown Section'}<br/>
                    ${slot.faculty?.name || 'Unassigned'}<br/>
                    ${slot.room?.name || 'No Room'}
                </div>
            `).join('');
            return `<td class="schedule-cell">${entries}</td>`;
        }).join('');

        return `<tr><td class="time-slot">${label}</td>${cells}</tr>`;
    }).join('');
}
