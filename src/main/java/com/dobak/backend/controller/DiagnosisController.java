package com.dobak.backend.controller;

import com.dobak.backend.dto.DiagnosisQuestion;
import com.dobak.backend.dto.DiagnosisResult;
import com.dobak.backend.dto.DiagnosisSubmitRequest;
import com.dobak.backend.service.DiagnosisService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/diagnose")
public class DiagnosisController {

    private final DiagnosisService diagnosisService;

    public DiagnosisController(DiagnosisService diagnosisService) {
        this.diagnosisService = diagnosisService;
    }

    /** B모드: 3분 진단용 문항 조회 */
    @GetMapping("/questions")
    public List<DiagnosisQuestion> getQuestions() {
        return diagnosisService.getQuestions();
    }

    /** B모드: 진단 답변 제출 -> 오류 유형 분류 결과 (ErrorPattern에 누적) */
    @PostMapping("/submit")
    public DiagnosisResult submit(@RequestBody DiagnosisSubmitRequest request) {
        return diagnosisService.submit(request);
    }
}
