package com.dobak.backend.dto;

/**
 * errorType 예시: phonological(음운), visual(시각적 주의 폭), letter_reversal(글자 뒤집힘)
 */
public record DiagnosisResult(String errorType, String description, String recommendedLevel) {
}
