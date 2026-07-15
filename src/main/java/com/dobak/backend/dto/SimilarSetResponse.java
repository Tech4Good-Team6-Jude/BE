package com.dobak.backend.dto;

import java.util.List;

/**
 * sourceType: "CAPTURE"(사진 캡처 흐름) / "BOOK"(도서관 흐름) — sourceSentenceId를 어느 테이블에서
 * 찾아야 하는지 FE가 구분할 수 있게 함께 내려준다.
 * eggReward: 문항 완료(completeItem) 호출일 때만 채워짐(알 획득/부화 여부) — 세트 생성/재조회 시엔 null.
 */
public record SimilarSetResponse(
        Long setId,
        Long sourceSentenceId,
        String sourceType,
        String pattern,
        String difficulty,
        List<SimilarSentenceSummary> sentences,
        EggRewardInfo eggReward
) {
}
