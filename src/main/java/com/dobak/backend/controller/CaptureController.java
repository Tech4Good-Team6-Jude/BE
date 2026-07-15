package com.dobak.backend.controller;

import com.dobak.backend.dto.CaptureResponse;
import com.dobak.backend.dto.CaptureSentenceSummary;
import com.dobak.backend.dto.EditSentenceRequest;
import com.dobak.backend.service.CaptureService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/captures")
public class CaptureController {

    private final CaptureService captureService;

    public CaptureController(CaptureService captureService) {
        this.captureService = captureService;
    }

    /**
     * 사진/이미지를 업로드하면 OCR + 문장 단위 분할 결과를 반환한다.
     * 반환된 sessionId는 이후 이해보조/유사문장/발음점검 호출에 세션 anchor로 계속 실어보낸다.
     */
    @PostMapping(consumes = "multipart/form-data")
    public CaptureResponse createCapture(@RequestParam("childId") Long childId,
                                          @RequestParam("file") MultipartFile file) {
        return captureService.createCapture(childId, file);
    }

    /** OCR 오인식 문장을 사용자가 직접 수정 */
    @PatchMapping("/sentences/{sentenceId}")
    public CaptureSentenceSummary editSentence(@PathVariable Long sentenceId,
                                                @RequestBody EditSentenceRequest request) {
        return captureService.editSentence(sentenceId, request.text());
    }
}
