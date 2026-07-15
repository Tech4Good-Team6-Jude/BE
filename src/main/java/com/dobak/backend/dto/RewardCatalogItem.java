package com.dobak.backend.dto;

/** 부화 리워드로 고를 수 있는 상품 하나. validDays: 발급일로부터 유효기간(일) */
public record RewardCatalogItem(String code, String brand, String name, String imageUrl, int validDays) {
}
