package com.dobak.backend.inference.dto;

import java.util.List;

public record PronunciationEvalResult(String sttText, double accuracy, List<String> misreadWords) {
}
