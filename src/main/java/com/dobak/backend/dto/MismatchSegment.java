package com.dobak.backend.dto;

/**
 * 발음점검 불일치 구간 하나.
 * correctionType: "받침" / "발음" / "띄어읽기" 등 오류 유형
 * modelAudioUrl: 이 단어만 정확하게 발음한 시범 TTS (오류구간만 선택 재생용)
 */
public record MismatchSegment(String expectedWord, String heardAs, String correctionType, String modelAudioUrl) {
}
