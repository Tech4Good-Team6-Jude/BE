package com.dobak.backend.dto;

/** label: "월"/"화"/... 이번 주 요일. checked: 그날 발음점검을 1회 이상 했는지 */
public record DayCheckIn(String label, boolean checked) {
}
