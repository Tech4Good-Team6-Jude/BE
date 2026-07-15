package com.dobak.backend.controller;

import com.dobak.backend.dto.ChildReportResponse;
import com.dobak.backend.dto.ReportResponse;
import com.dobak.backend.dto.ReportSummary;
import com.dobak.backend.service.ReportService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    /** 보호자용 최신 리포트 생성/조회 (부모 리포트 화면) */
    @GetMapping("/children/{childId}/report")
    public ReportResponse getLatestReport(@PathVariable Long childId) {
        return reportService.generateLatest(childId);
    }

    /** 아이 본인용 리포트 ("나의 읽기" 화면) — 스트릭/요일별 도장/소리연습 패턴 포함 */
    @GetMapping("/children/{childId}/report/child")
    public ChildReportResponse getChildReport(@PathVariable Long childId) {
        return reportService.getChildReport(childId);
    }

    /** 레포트 목록(사이드바) — 지난 주차들 */
    @GetMapping("/children/{childId}/reports")
    public List<ReportSummary> listReports(@PathVariable Long childId) {
        return reportService.listReports(childId);
    }

    /** 레포트 목록에서 과거 리포트 하나를 클릭했을 때 그 상세(발음비교 포함)를 보여줌 */
    @GetMapping("/reports/{reportId}")
    public ReportResponse getReport(@PathVariable Long reportId) {
        return reportService.getReport(reportId);
    }

    /** 보호자가 "칭찬 도장 보내기" 버튼 눌렀을 때 */
    @PostMapping("/reports/{reportId}/stamp")
    public void sendStamp(@PathVariable Long reportId) {
        reportService.sendStamp(reportId);
    }
}
