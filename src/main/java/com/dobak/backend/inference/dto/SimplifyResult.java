package com.dobak.backend.inference.dto;

import com.dobak.backend.dto.KeyWordDetail;

import java.util.List;

/**
 * simplifiedText: 쉬워진 문장
 * keyWords: 원문에서 이 재설명이 필요했던 이유(근거)가 된 핵심 단어 + 그 뜻 — 기능명세서 2.2.2 "근거 단서 제공".
 *           FE는 원문 표시 시 이 단어들을 하이라이트해서 "이 단어 때문에 이렇게 바꿨어요"를 보여줄 수 있다.
 */
public record SimplifyResult(String simplifiedText, List<KeyWordDetail> keyWords) {
}
