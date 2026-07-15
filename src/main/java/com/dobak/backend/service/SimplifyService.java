package com.dobak.backend.service;

import com.dobak.backend.dto.SimplifyResponse;
import com.dobak.backend.entity.ReadingSession;
import com.dobak.backend.entity.User;
import com.dobak.backend.inference.InferenceClient;
import com.dobak.backend.inference.dto.TtsResult;
import com.dobak.backend.repository.ReadingSessionRepository;
import com.dobak.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class SimplifyService {

    private final InferenceClient inferenceClient;
    private final ReadingSessionRepository readingSessionRepository;
    private final UserRepository userRepository;

    public SimplifyService(InferenceClient inferenceClient,
                            ReadingSessionRepository readingSessionRepository,
                            UserRepository userRepository) {
        this.inferenceClient = inferenceClient;
        this.readingSessionRepository = readingSessionRepository;
        this.userRepository = userRepository;
    }

    /**
     * A모드 핵심 파이프라인: OCR -> 문장단순화 -> TTS 순으로 Inference Server를 호출하고
     * 결과를 ReadingSession으로 저장한다. (지금은 InferenceClient가 mock 응답을 줌)
     */
    public SimplifyResponse process(Long childId, MultipartFile file) {
        User child = userRepository.findById(childId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이 계정: " + childId));

        String originalText = inferenceClient.ocr(file);
        String simplifiedText = inferenceClient.simplify(originalText);
        TtsResult ttsResult = inferenceClient.tts(simplifiedText);

        ReadingSession session = new ReadingSession(child, originalText, simplifiedText, ttsResult.audioUrl());
        readingSessionRepository.save(session);

        return new SimplifyResponse(
                session.getId(),
                originalText,
                simplifiedText,
                ttsResult.audioUrl(),
                ttsResult.words()
        );
    }
}
