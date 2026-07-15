package com.dobak.backend.config;

import com.dobak.backend.entity.GuardianChildLink;
import com.dobak.backend.entity.User;
import com.dobak.backend.entity.UserRole;
import com.dobak.backend.repository.GuardianChildLinkRepository;
import com.dobak.backend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 회원가입/로그인 API(AuthController)가 아직 없어서, 그 전까지 Postman 등에서
 * childId가 필요한 API를 바로 테스트할 수 있도록 데모 유저를 시드해둔다.
 * IDENTITY 채번이라 빈 DB 기준으로 child가 1번, guardian이 2번을 받는다.
 * TODO(Auth): signup/login/link 구현되면 이 시더는 지워도 됨.
 */
@Component
@Order(1)
public class UserSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final GuardianChildLinkRepository guardianChildLinkRepository;

    public UserSeeder(UserRepository userRepository, GuardianChildLinkRepository guardianChildLinkRepository) {
        this.userRepository = userRepository;
        this.guardianChildLinkRepository = guardianChildLinkRepository;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }

        User child = userRepository.save(new User("child1@test.com", "1234", "민준", UserRole.CHILD));
        User guardian = userRepository.save(new User("guardian1@test.com", "1234", "민준맘", UserRole.GUARDIAN));
        guardianChildLinkRepository.save(new GuardianChildLink(guardian, child));
    }
}
