package com.dobak.backend.dto;

import java.util.List;

/**
 * sessionId: 생성된 ReadingSession id — 이후 이해보조(explain)/유사문장/발음점검 호출 시
 *            이 id를 세션 anchor로 같이 실어보내서 하나의 학습 흐름으로 리포트에 묶인다.
 */
public record CaptureResponse(
        Long sessionId,
        String imageUrl,
        List<CaptureSentenceSummary> sentences
) {
}
