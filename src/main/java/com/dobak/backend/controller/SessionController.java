package com.dobak.backend.controller;

import com.dobak.backend.dto.ExplainRequest;
import com.dobak.backend.dto.ExplainResponse;
import com.dobak.backend.service.SessionService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sessions")
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    /** 읽는 중 드래그로 선택한 부분을 쉬운 문장으로 재설명 + TTS */
    @PostMapping("/{sessionId}/explain")
    public ExplainResponse explain(@PathVariable Long sessionId, @RequestBody ExplainRequest request) {
        return sessionService.explain(sessionId, request.selectedText());
    }
}
