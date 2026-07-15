package com.dobak.backend.dto;

/**
 * 짝맞추기 라운드에 낼 정답 쌍 하나. FE는 이 하나로 카드 2장(소리 카드=audioUrl, 글자 카드=word)을
 * 만들어서 pairId를 함께 붙여두고, 전체 카드를 섞어서 화면에 보여준다.
 */
public record WordMatchPairSummary(Long pairId, String word, String audioUrl) {
}
