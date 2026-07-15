package com.dobak.backend.dto;

/**
 * 재설명 근거가 된 핵심 단어 하나 + 그 뜻.
 * FE는 word로 원문 하이라이트를 하고, meaning으로 "'{word}'는 ~라는 뜻이에요" 같은 보조 설명을 보여줄 수 있다.
 */
public record KeyWordDetail(String word, String meaning) {
}
