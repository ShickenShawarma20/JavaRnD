package com.javard.eams.modules.report.controller;

import com.javard.eams.modules.report.service.ExcelReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDate;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
@Tag(name = "Reports & Analytics", description = "Endpoints for downloading Excel/CSV attendance summaries")
public class ReportController {

    private final ExcelReportService excelReportService;

    @GetMapping("/daily/excel")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HR_ADMIN')")
    @Operation(summary = "Export daily attendance sheet as formatted Excel (.xlsx)")
    public ResponseEntity<byte[]> downloadDailyExcel(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) throws IOException {
        LocalDate targetDate = (date != null) ? date : LocalDate.now();
        byte[] excelBytes = excelReportService.generateDailyAttendanceExcel(targetDate);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=attendance_" + targetDate + ".xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelBytes);
    }
}

