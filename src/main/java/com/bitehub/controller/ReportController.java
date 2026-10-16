package com.bitehub.controller;

import com.bitehub.dto.RestaurantPerformanceDTO;
import com.bitehub.dto.SalesReportDTO;
import com.bitehub.dto.TopItemDTO;
import com.bitehub.service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@PreAuthorize("hasAnyRole('ADMIN', 'RESTAURANT')")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/daily")
    public ResponseEntity<List<SalesReportDTO>> getDailyReport() {
        return ResponseEntity.ok(reportService.getDailyOrdersReport());
    }

    @GetMapping("/weekly")
    public ResponseEntity<List<SalesReportDTO>> getWeeklyReport() {
        return ResponseEntity.ok(reportService.getWeeklyOrdersReport());
    }

    @GetMapping("/monthly")
    public ResponseEntity<List<SalesReportDTO>> getMonthlyReport() {
        return ResponseEntity.ok(reportService.getMonthlyOrdersReport());
    }

    @GetMapping("/top-items")
    public ResponseEntity<List<TopItemDTO>> getTopItems(@RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(reportService.getMostOrderedFoodItems(limit));
    }

    @GetMapping("/restaurant-performance")
    public ResponseEntity<List<RestaurantPerformanceDTO>> getRestaurantPerformance() {
        return ResponseEntity.ok(reportService.getRestaurantPerformance());
    }
}
