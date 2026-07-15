package com.dobak.backend.dto;

import java.util.List;

public record DiagnosisSubmitRequest(Long childId, List<DiagnosisAnswer> answers) {
}
