package com.dobak.backend.controller;

import com.dobak.backend.dto.ChildProfileResponse;
import com.dobak.backend.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 아이/보호자 기본 프로필 조회. 로그인 자체는 이번 범위 밖이라 이름 조회만 담당한다. */
@RestController
@RequestMapping("/api/v1")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /** 아이 이름 등 기본 프로필 — 리포트/도서관 화면 헤더에 "{name}이의 ..." 문구 조립용 */
    @GetMapping("/children/{childId}")
    public ChildProfileResponse getChildProfile(@PathVariable Long childId) {
        return userService.getChildProfile(childId);
    }
}
