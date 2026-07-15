package com.dobak.backend.dto;

import com.dobak.backend.entity.UserRole;

public record AuthResponse(Long userId, String name, UserRole role) {
}
