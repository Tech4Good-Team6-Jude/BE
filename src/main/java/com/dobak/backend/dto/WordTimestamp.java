package com.dobak.backend.dto;

/**
 * TTS 낭독 중 노래방식 하이라이트를 위한 단어별 시간 구간.
 */
public record WordTimestamp(String word, long startMs, long endMs) {
}
