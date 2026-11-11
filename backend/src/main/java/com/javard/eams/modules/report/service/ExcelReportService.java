package com.javard.eams.modules.report.service;

import com.javard.eams.modules.attendance.entity.AttendanceSession;
import com.javard.eams.modules.attendance.repository.AttendanceSessionRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExcelReportService {

    private final AttendanceSessionRepository sessionRepository;

    public byte[] generateDailyAttendanceExcel(LocalDate date) throws IOException {
        List<AttendanceSession> sessions = sessionRepository.findAll().stream()
                .filter(s -> s.getWorkDate().equals(date))
                .toList();

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Daily Attendance - " + date);

            // Header Style
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(headerFont);
            headerCellStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Columns
            String[] columns = {"Emp Code", "Employee Name", "Department", "Shift", "Status", "Check In", "Check Out", "Gross (Mins)", "Net (Mins)", "Late (Mins)", "Early (Mins)", "OT (Mins)"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerCellStyle);
            }

            int rowIdx = 1;
            for (AttendanceSession s : sessions) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(s.getEmployee().getEmployeeCode());
                row.createCell(1).setCellValue(s.getEmployee().getFullName());
                row.createCell(2).setCellValue(s.getEmployee().getDepartment().getDepartmentName());
                row.createCell(3).setCellValue(s.getShift().getShiftName());
                row.createCell(4).setCellValue(s.getStatus().name());
                row.createCell(5).setCellValue(s.getFirstCheckIn() != null ? s.getFirstCheckIn().toString() : "-");
                row.createCell(6).setCellValue(s.getLastCheckOut() != null ? s.getLastCheckOut().toString() : "-");
                row.createCell(7).setCellValue(s.getGrossMinutes());
                row.createCell(8).setCellValue(s.getNetWorkMinutes());
                row.createCell(9).setCellValue(s.getLateEntryMinutes());
                row.createCell(10).setCellValue(s.getEarlyExitMinutes());
                row.createCell(11).setCellValue(s.getOvertimeMinutes());
            }

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }
}
