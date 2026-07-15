package com.dobak.backend.service;

import com.dobak.backend.dto.ExplainResponse;
import com.dobak.backend.entity.ReadingSession;
import com.dobak.backend.entity.UnknownWordQuery;
import com.dobak.backend.inference.InferenceClient;
import com.dobak.backend.inference.dto.TtsResult;
import com.dobak.backend.repository.ReadingSessionRepository;
import com.dobak.backend.repository.UnknownWordQueryRepository;
import org.springframework.stereotype.Service;

@Service
public class SessionService {

    private final InferenceClient inferenceClient;
    private final ReadingSessionRepository readingSessionRepository;
    private final UnknownWordQueryRepository unknownWordQueryRepository;

    public SessionService(InferenceClient inferenceClient,
                           ReadingSessionRepository readingSessionRepository,
                           UnknownWordQueryRepository unknownWordQueryRepository) {
        this.inferenceClient = inferenceClient;
        this.readingSessionRepository = readingSessionRepository;
        this.unknownWordQueryRepository = unknownWordQueryRepository;
    }

    /**
     * 드래그로 선택한 "모르는 부분"을 쉬운 문장으로 재설명 + TTS.
     * 결과는 UnknownWordQuery로 저장되어 이후 오류 패턴(공통분모) 분석의 재료가 됨.
     */
    public ExplainResponse explain(Long sessionId, String selectedText) {
        ReadingSession session = readingSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 세션: " + sessionId));

        String explanation = inferenceClient.simplify(selectedText);
        TtsResult tts = inferenceClient.tts(explanation);

        UnknownWordQuery query = new UnknownWordQuery(session, selectedText, explanation, tts.audioUrl());
        unknownWordQueryRepository.save(query);

        return new ExplainResponse(query.getId(), selectedText, explanation, tts.audioUrl());
    }
}
