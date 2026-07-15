package com.dobak.backend.controller;

import com.dobak.backend.dto.ReportResponse;
import com.dobak.backend.service.ReportService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/children/{childId}/report")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    /** 보호자용 리포트 (최근 7일 발음 이력 + 오류 유형 요약) 생성/조회 */
    @GetMapping
    public ReportResponse getReport(@PathVariable Long childId) {
        return reportService.generateLatest(childId);
    }
}
