package com.uniplanner.api;

import com.uniplanner.UniPlannerController;
import com.uniplanner.models.ScheduleSlot;
import com.uniplanner.models.Timetable;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ========================================================================
 * Export Controller — Excel and CSV timetable exports
 * ========================================================================
 * Provides endpoints:
 *   GET /api/timetable/export/excel           — full timetable as .xlsx
 *   GET /api/timetable/export/excel/{section} — single section as .xlsx
 *   GET /api/timetable/export/csv             — full timetable as .csv
 *   GET /api/timetable/export/csv/{section}   — single section as .csv
 * ========================================================================
 */
@RestController
@RequestMapping("/api/timetable/export")
@CrossOrigin(origins = "*")
public class ExportController {

    private final UniPlannerController controller;

    @Autowired
    public ExportController(UniPlannerController controller) {
        this.controller = controller;
    }

    // ============== EXCEL EXPORT ==============

    /**
     * Export full timetable as Excel (.xlsx)
     */
    @GetMapping("/excel")
    public ResponseEntity<byte[]> exportFullTimetableExcel() throws IOException {
        Timetable timetable = controller.getCurrentTimetable();
        if (timetable == null || timetable.getScheduledSlots().isEmpty()) {
            return ResponseEntity.badRequest().body("No timetable generated yet. Please generate first.".getBytes());
        }
        return buildExcelResponse(timetable.getScheduledSlots(), "timetable_full");
    }

    /**
     * Export a single section's timetable as Excel (.xlsx)
     */
    @GetMapping("/excel/{sectionId}")
    public ResponseEntity<byte[]> exportSectionTimetableExcel(@PathVariable String sectionId) throws IOException {
        List<ScheduleSlot> slots = controller.getTimetableForSection(sectionId);
        if (slots.isEmpty()) {
            return ResponseEntity.badRequest().body(("No timetable found for section: " + sectionId).getBytes());
        }
        return buildExcelResponse(slots, "timetable_" + sectionId);
    }

    // ============== CSV EXPORT ==============

    /**
     * Export full timetable as CSV
     */
    @GetMapping("/csv")
    public ResponseEntity<byte[]> exportFullTimetableCsv() {
        Timetable timetable = controller.getCurrentTimetable();
        if (timetable == null || timetable.getScheduledSlots().isEmpty()) {
            return ResponseEntity.badRequest().body("No timetable generated yet.".getBytes());
        }
        return buildCsvResponse(timetable.getScheduledSlots(), "timetable_full");
    }

    /**
     * Export a single section's timetable as CSV
     */
    @GetMapping("/csv/{sectionId}")
    public ResponseEntity<byte[]> exportSectionTimetableCsv(@PathVariable String sectionId) {
        List<ScheduleSlot> slots = controller.getTimetableForSection(sectionId);
        if (slots.isEmpty()) {
            return ResponseEntity.badRequest().body(("No timetable found for section: " + sectionId).getBytes());
        }
        return buildCsvResponse(slots, "timetable_" + sectionId);
    }

    // ============== BUILDERS ==============

    private ResponseEntity<byte[]> buildExcelResponse(List<ScheduleSlot> slots, String filePrefix)
            throws IOException {

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            // ---- Styles ----
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle titleStyle = createTitleStyle(workbook);
            CellStyle validStyle = createValidStyle(workbook);
            CellStyle conflictStyle = createConflictStyle(workbook);
            CellStyle subHeaderStyle = createSubHeaderStyle(workbook);

            // ---- Sheet: Full Timetable ----
            Sheet sheet = workbook.createSheet("Timetable");
            sheet.setColumnWidth(0, 5000);
            sheet.setColumnWidth(1, 6000);
            sheet.setColumnWidth(2, 7000);
            sheet.setColumnWidth(3, 7000);
            sheet.setColumnWidth(4, 5000);
            sheet.setColumnWidth(5, 5000);
            sheet.setColumnWidth(6, 4000);

            int rowNum = 0;

            // Title row
            Row titleRow = sheet.createRow(rowNum++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("UniPlanner — Timetable Export");
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 6));

            // Generated timestamp
            Row tsRow = sheet.createRow(rowNum++);
            tsRow.createCell(0).setCellValue("Generated: " +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 6));
            rowNum++; // blank row

            // Column headers
            Row headerRow = sheet.createRow(rowNum++);
            String[] columns = {"Section", "Subject", "Faculty", "Room", "Day", "Time", "Activity"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data rows — group by section using sub-headers
            String lastSection = null;
            for (ScheduleSlot slot : sortSlots(slots)) {
                String sectionName = slot.getSection() != null ? slot.getSection().getSectionName() : "N/A";

                // Sub-header when section changes
                if (!sectionName.equals(lastSection)) {
                    Row subHdr = sheet.createRow(rowNum++);
                    Cell subCell = subHdr.createCell(0);
                    subCell.setCellValue("▶  Section: " + sectionName);
                    subCell.setCellStyle(subHeaderStyle);
                    sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 6));
                    lastSection = sectionName;
                }

                Row row = sheet.createRow(rowNum++);
                boolean isConflict = slot.hasConflicts();
                CellStyle rowStyle = isConflict ? conflictStyle : validStyle;

                setCell(row, 0, sectionName, rowStyle);
                setCell(row, 1, slot.getSubject() != null ? slot.getSubject().getName() : "N/A", rowStyle);
                setCell(row, 2, slot.getFaculty() != null ? slot.getFaculty().getName() : "N/A", rowStyle);
                setCell(row, 3, slot.getRoom() != null ? slot.getRoom().getName() : "N/A", rowStyle);
                setCell(row, 4, slot.getTimeSlot() != null ? slot.getTimeSlot().getDay().toString() : "N/A", rowStyle);
                setCell(row, 5, slot.getTimeSlot() != null ?
                        slot.getTimeSlot().getStartTime() + "-" + slot.getTimeSlot().getEndTime() : "N/A", rowStyle);
                setCell(row, 6, slot.getActivityType() != null ? slot.getActivityType().toString() : "N/A", rowStyle);
            }

            // Auto-filter on header row
            sheet.setAutoFilter(new CellRangeAddress(3, 3, 0, 6));

            // ---- Sheet: Statistics ----
            Sheet statsSheet = workbook.createSheet("Statistics");
            statsSheet.setColumnWidth(0, 7000);
            statsSheet.setColumnWidth(1, 5000);

            Row statsTitle = statsSheet.createRow(0);
            Cell statsTitleCell = statsTitle.createCell(0);
            statsTitleCell.setCellValue("Timetable Statistics");
            statsTitleCell.setCellStyle(titleStyle);
            statsSheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 1));

            int sr = 2;
            long validSlots = slots.stream().filter(s -> !s.hasConflicts()).count();
            long conflictSlots = slots.stream().filter(ScheduleSlot::hasConflicts).count();

            addStatRow(statsSheet, sr++, "Total Slots", String.valueOf(slots.size()));
            addStatRow(statsSheet, sr++, "Valid Slots", String.valueOf(validSlots));
            addStatRow(statsSheet, sr++, "Conflict Slots", String.valueOf(conflictSlots));
            addStatRow(statsSheet, sr++, "Total Faculty", String.valueOf(controller.getFaculties().size()));
            addStatRow(statsSheet, sr++, "Total Sections", String.valueOf(controller.getSections().size()));
            addStatRow(statsSheet, sr++, "Total Rooms", String.valueOf(controller.getSpaces().size()));

            // Write to byte array
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            workbook.write(baos);
            byte[] excelBytes = baos.toByteArray();

            String filename = filePrefix + "_" +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")) + ".xlsx";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .contentLength(excelBytes.length)
                    .body(excelBytes);
        }
    }

    private ResponseEntity<byte[]> buildCsvResponse(List<ScheduleSlot> slots, String filePrefix) {
        StringBuilder csv = new StringBuilder();
        csv.append("Section,Subject,Faculty,Room,Day,Start Time,End Time,Activity,Status\n");

        for (ScheduleSlot slot : sortSlots(slots)) {
            csv.append(escape(slot.getSection() != null ? slot.getSection().getSectionName() : "N/A")).append(",");
            csv.append(escape(slot.getSubject() != null ? slot.getSubject().getName() : "N/A")).append(",");
            csv.append(escape(slot.getFaculty() != null ? slot.getFaculty().getName() : "N/A")).append(",");
            csv.append(escape(slot.getRoom() != null ? slot.getRoom().getName() : "N/A")).append(",");
            csv.append(slot.getTimeSlot() != null ? slot.getTimeSlot().getDay() : "N/A").append(",");
            csv.append(slot.getTimeSlot() != null ? slot.getTimeSlot().getStartTime() : "N/A").append(",");
            csv.append(slot.getTimeSlot() != null ? slot.getTimeSlot().getEndTime() : "N/A").append(",");
            csv.append(slot.getActivityType() != null ? slot.getActivityType() : "N/A").append(",");
            csv.append(slot.hasConflicts() ? "CONFLICT" : "VALID").append("\n");
        }

        byte[] csvBytes = csv.toString().getBytes();
        String filename = filePrefix + "_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")) + ".csv";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .contentLength(csvBytes.length)
                .body(csvBytes);
    }

    // ============== STYLE HELPERS ==============

    private CellStyle createTitleStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle createHeaderStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_TEAL.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle createSubHeaderStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private CellStyle createValidStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBottomBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        return style;
    }

    private CellStyle createConflictStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setColor(IndexedColors.DARK_RED.getIndex());
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.ROSE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        return style;
    }

    private void setCell(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private void addStatRow(Sheet sheet, int rowNum, String label, String value) {
        Row row = sheet.createRow(rowNum);
        row.createCell(0).setCellValue(label);
        row.createCell(1).setCellValue(value);
    }

    // Sort slots by section, then by day, then by start time
    private List<ScheduleSlot> sortSlots(List<ScheduleSlot> slots) {
        return slots.stream()
                .filter(s -> s.getSection() != null && s.getTimeSlot() != null)
                .sorted((a, b) -> {
                    int sectionCmp = a.getSection().getSectionName()
                            .compareTo(b.getSection().getSectionName());
                    if (sectionCmp != 0) return sectionCmp;
                    int dayCmp = a.getTimeSlot().getDay().compareTo(b.getTimeSlot().getDay());
                    if (dayCmp != 0) return dayCmp;
                    return a.getTimeSlot().getStartTime().compareTo(b.getTimeSlot().getStartTime());
                })
                .collect(Collectors.toList());
    }

    // Escape CSV values with commas/quotes
    private String escape(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
