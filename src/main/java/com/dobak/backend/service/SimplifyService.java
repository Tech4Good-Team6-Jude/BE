package com.dobak.backend.service;

import com.dobak.backend.dto.SimplifyResponse;
import com.dobak.backend.dto.WordTimestamp;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class SimplifyService {

    /**
     * A모드 핵심 파이프라인.
     * TODO(AI팀 연동 지점):
     *   1) OCR: file -> originalText          (Vision API 등)
     *   2) LLM 재작성: originalText -> simplifiedText
     *   3) TTS: simplifiedText -> audioUrl + word-level timestamps
     * 지금은 FE가 먼저 화면을 붙일 수 있도록 mock 데이터를 반환한다.
     * AI팀 결과물이 준비되면 이 메서드 내부만 실제 호출로 교체하면 됨
     * (컨트롤러/DTO 계약은 그대로 유지).
     */
    public SimplifyResponse process(MultipartFile file) {
        String originalText = "원본 텍스트 (OCR 결과가 들어올 자리)";
        String simplifiedText = "쉬운 문장으로 바뀐 텍스트입니다.";

        List<WordTimestamp> words = List.of(
                new WordTimestamp("쉬운", 0, 400),
                new WordTimestamp("문장으로", 400, 900),
                new WordTimestamp("바뀐", 900, 1200),
                new WordTimestamp("텍스트입니다.", 1200, 1800)
        );

        return new SimplifyResponse(
                originalText,
                simplifiedText,
                "https://example.com/mock-audio.mp3",
                words
        );
    }
}
