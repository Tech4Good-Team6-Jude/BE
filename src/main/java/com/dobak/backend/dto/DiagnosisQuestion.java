package com.dobak.backend.dto;

/**
 * type 예시: read_aloud(소리내어 읽기), phoneme_match(음운 인식),
 * visual_span(시각적 주의 폭), comprehension(독해), letter_reversal(글자 뒤집힘)
 */
public record DiagnosisQuestion(long id, String prompt, String type) {
}
