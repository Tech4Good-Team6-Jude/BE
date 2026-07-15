package com.dobak.backend.controller;

import com.dobak.backend.dto.PracticeAttemptResponse;
import com.dobak.backend.service.PracticeService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/practice")
public class PracticeController {

    private final PracticeService practiceService;

    public PracticeController(PracticeService practiceService) {
        this.practiceService = practiceService;
    }

    /** B모드: 발음 연습/점검 녹음 제출 (듀오링고식 따라읽기) */
    @PostMapping(value = "/attempts", consumes = "multipart/form-data")
    public PracticeAttemptResponse submit(@RequestParam("childId") Long childId,
                                           @RequestParam("targetText") String targetText,
                                           @RequestParam("audio") MultipartFile audio,
                                           @RequestParam(value = "compareToAttemptId", required = false) Long compareToAttemptId) {
        return practiceService.submit(childId, targetText, audio, compareToAttemptId);
    }
}
