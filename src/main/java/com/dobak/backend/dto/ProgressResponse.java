package com.dobak.backend.dto;

public record ProgressResponse(int grapeCount, int currentBunchCount, int totalBunchesCompleted, int grapesPerBunch) {
}
