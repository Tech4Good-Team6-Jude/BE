package com.dobak.backend.dto;

import com.dobak.backend.entity.UserRole;

public record SignupRequest(String email, String password, String name, UserRole role) {
}
