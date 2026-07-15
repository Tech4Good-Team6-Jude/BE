package com.dobak.backend.config;

import com.dobak.backend.entity.Book;
import com.dobak.backend.entity.BookSentence;
import com.dobak.backend.entity.StuckSentence;
import com.dobak.backend.entity.User;
import com.dobak.backend.inference.InferenceClient;
import com.dobak.backend.repository.BookRepository;
import com.dobak.backend.repository.BookSentenceRepository;
import com.dobak.backend.repository.StuckSentenceRepository;
import com.dobak.backend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * 데모용 테스트 계정(childId=1, child1@test.com)이 "미운 아기 오리" 3쪽의 4문장을 이미 전부
 * "막힌 문장"으로 표시해둔 상태로 서버가 뜨게 한다. 시연에서는 실제 카메라로 촬영하지 않고
 * 프론트가 준비된 사진(3쪽 화면)을 보여주는 것뿐이라, 그 자리에서 문장을 하나하나 눌러 막힌
 * 표시를 하는 라이브 조작 없이도 다음 화면(L4 막힌문장 목록/L5 반복학습)이 바로 4문장 전부
 * 채워진 상태로 넘어가야 해서 이렇게 미리 시드해둔다.
 */
@Component
@Order(3)
public class DemoStuckSentenceSeeder implements CommandLineRunner {

    private static final String DEMO_CHILD_EMAIL = "child1@test.com";
    private static final String DEMO_BOOK_TITLE = "미운 아기 오리";
    private static final List<String> DEMO_STUCK_TEXTS = List.of(
            "형제들도, 다른 동물들도 그를 미워했어요.",
            "'넌 너무 못생겼어!'",
            "참다못한 아기 오리는 집을 떠났어요.",
            "외로운 오리는 깊은 숲 속으로 걸어갔답니다."
    );

    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final BookSentenceRepository bookSentenceRepository;
    private final StuckSentenceRepository stuckSentenceRepository;
    private final InferenceClient inferenceClient;

    public DemoStuckSentenceSeeder(UserRepository userRepository, BookRepository bookRepository,
                                    BookSentenceRepository bookSentenceRepository,
                                    StuckSentenceRepository stuckSentenceRepository,
                                    InferenceClient inferenceClient) {
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
        this.bookSentenceRepository = bookSentenceRepository;
        this.stuckSentenceRepository = stuckSentenceRepository;
        this.inferenceClient = inferenceClient;
    }

    @Override
    public void run(String... args) {
        if (stuckSentenceRepository.count() > 0) {
            return;
        }

        Optional<User> child = userRepository.findByEmail(DEMO_CHILD_EMAIL);
        Optional<Book> book = bookRepository.findByTitle(DEMO_BOOK_TITLE);
        if (child.isEmpty() || book.isEmpty()) {
            return;
        }

        List<BookSentence> allSentences = bookSentenceRepository
                .findByBookIdOrderByPageIndexAscOrderIndexAsc(book.get().getId());

        for (String text : DEMO_STUCK_TEXTS) {
            allSentences.stream()
                    .filter(s -> s.getText().equals(text))
                    .findFirst()
                    .ifPresent(sentence -> {
                        String pattern = inferenceClient.analyzeSentencePattern(sentence.getText());
                        stuckSentenceRepository.save(
                                new StuckSentence(child.get(), book.get(), sentence, sentence.getText(), pattern)
                        );
                    });
        }
    }
}
