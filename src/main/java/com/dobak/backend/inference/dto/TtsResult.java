package com.dobak.backend.inference.dto;

import com.dobak.backend.dto.WordTimestamp;

import java.util.List;

public record TtsResult(String audioUrl, List<WordTimestamp> words) {
}
