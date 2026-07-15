package com.dobak.backend.controller;

import com.dobak.backend.dto.AuthResponse;
import com.dobak.backend.dto.LinkGuardianChildRequest;
import com.dobak.backend.dto.LoginRequest;
import com.dobak.backend.dto.SignupRequest;
import com.dobak.backend.service.AuthService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /** 아이/보호자 공통 회원가입 (role로 구분) */
    @PostMapping("/signup")
    public AuthResponse signup(@RequestBody SignupRequest request) {
        return authService.signup(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    /** 보호자 계정과 아이 계정을 연결 -> 필요할까*/
    @PostMapping("/link")
    public void link(@RequestBody LinkGuardianChildRequest request) {
        authService.linkGuardianAndChild(request);
    }
}
