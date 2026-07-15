package com.dobak.backend.dto;

import java.util.List;

public record DiagnosisSubmitRequest(List<DiagnosisAnswer> answers) {
}
