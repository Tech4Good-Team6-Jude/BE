package com.dobak.backend.service;

import com.dobak.backend.dto.AuthResponse;
import com.dobak.backend.dto.LinkGuardianChildRequest;
import com.dobak.backend.dto.LoginRequest;
import com.dobak.backend.dto.SignupRequest;
import com.dobak.backend.entity.GuardianChildLink;
import com.dobak.backend.entity.User;
import com.dobak.backend.repository.GuardianChildLinkRepository;
import com.dobak.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

/**
 * 해커톤 단계 간이 인증. 비밀번호 평문 비교, 토큰 발급 없음 — 클라이언트가
 * 응답의 userId를 저장해뒀다가 이후 요청에 그대로 실어보내는 방식(TODO: 실제 배포 전 보안 강화).
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final GuardianChildLinkRepository guardianChildLinkRepository;

    public AuthService(UserRepository userRepository, GuardianChildLinkRepository guardianChildLinkRepository) {
        this.userRepository = userRepository;
        this.guardianChildLinkRepository = guardianChildLinkRepository;
    }

    public AuthResponse signup(SignupRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new IllegalArgumentException("이미 가입된 이메일: " + request.email());
        }
        User user = new User(request.email(), request.password(), request.name(), request.role());
        userRepository.save(user);
        return new AuthResponse(user.getId(), user.getName(), user.getRole());
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 계정입니다."));
        if (!user.getPassword().equals(request.password())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        return new AuthResponse(user.getId(), user.getName(), user.getRole());
    }

    public void linkGuardianAndChild(LinkGuardianChildRequest request) {
        User guardian = userRepository.findById(request.guardianId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 보호자: " + request.guardianId()));
        User child = userRepository.findById(request.childId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이: " + request.childId()));
        guardianChildLinkRepository.save(new GuardianChildLink(guardian, child));
    }
}
