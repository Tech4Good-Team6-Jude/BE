package com.dobak.backend.service;

import com.dobak.backend.dto.ChildProfileResponse;
import com.dobak.backend.entity.User;
import com.dobak.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /** 아이 기본 프로필 조회 — "{name}이의 이번 주 읽기" 같은 문구를 FE가 조립할 때 씀 */
    public ChildProfileResponse getChildProfile(Long childId) {
        User child = userRepository.findById(childId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이 계정: " + childId));
        return new ChildProfileResponse(child.getId(), child.getName());
    }
}
