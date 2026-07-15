package com.dobak.backend.controller;

import com.dobak.backend.dto.ProgressResponse;
import com.dobak.backend.service.ProgressService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/children/{childId}/progress")
public class ProgressController {

    private final ProgressService progressService;

    public ProgressController(ProgressService progressService) {
        this.progressService = progressService;
    }

    /** 포도알 현황 조회 */
    @GetMapping
    public ProgressResponse getProgress(@PathVariable Long childId) {
        return progressService.getProgress(childId);
    }

    /** 포도알 하나 지급 (수동 트리거용, 보통은 PracticeService가 자동 지급) */
    @PostMapping("/grape")
    public ProgressResponse addGrape(@PathVariable Long childId) {
        return progressService.addGrape(childId);
    }
}
