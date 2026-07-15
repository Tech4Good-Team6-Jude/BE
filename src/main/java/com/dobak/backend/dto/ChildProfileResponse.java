package com.dobak.backend.dto;

/** 아이 기본 프로필. "{name}이의 이번 주 읽기" 같은 문구를 FE가 조립할 때 씀. */
public record ChildProfileResponse(Long childId, String name) {
}
