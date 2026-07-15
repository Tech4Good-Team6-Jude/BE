package com.dobak.backend.inference.dto;

import com.dobak.backend.dto.MismatchSegment;

import java.util.List;

/**
 * pattern: 이 목표 문장이 속한 소리 패턴(예: "겹받침"/"된소리"/"긴문장").
 * 리포트의 "이번 주 소리 연습"/"자주 막힌 유형" 집계에 쓰인다.
 */
public record PronunciationEvalResult(String sttText, double accuracy, List<MismatchSegment> mismatches, String pattern) {
}
