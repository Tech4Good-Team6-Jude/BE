package com.dobak.backend.dto;

/** stuck=false로 토글(해제)한 경우 stuckSentence는 null */
public record MarkStuckResponse(boolean stuck, StuckSentenceSummary stuckSentence) {
}
