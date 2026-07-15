package com.dobak.backend.config;

import com.dobak.backend.entity.Book;
import com.dobak.backend.entity.BookSentence;
import com.dobak.backend.repository.BookRepository;
import com.dobak.backend.repository.BookSentenceRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 도서관 기능(T2~L6) 데모용 시드 데이터. 서버 기동 시 책이 하나도 없으면 4권을 채워 넣는다.
 * 미운 아기 오리는 실제 데모 화면(와이어프레임)에 나온 값 그대로 맞춰뒀고(3쪽 본문이 캡처 촬영
 * UI에서 그대로 보여주는 문장 4개 그 자체 — 앞뒤 줄거리를 임의로 지어내지 않음, totalPages/
 * estimatedMinutes만 표시용으로 12쪽/8분 유지), 나머지 3권은 난이도/분량/예상 소요시간이
 * 임의값이며 본문도 저작권 있는 원작 텍스트를 그대로 옮긴 것이 아니라 데모용으로 새로 쓴 짧은 문장이다.
 */
@Component
@Order(2)
public class BookDataSeeder implements CommandLineRunner {

    private final BookRepository bookRepository;
    private final BookSentenceRepository bookSentenceRepository;

    public BookDataSeeder(BookRepository bookRepository, BookSentenceRepository bookSentenceRepository) {
        this.bookRepository = bookRepository;
        this.bookSentenceRepository = bookSentenceRepository;
    }

    @Override
    public void run(String... args) {
        if (bookRepository.count() > 0) {
            return;
        }

        seedUglyDuckling();
        seedGiantTurnip();
        seedBabyBearBath();
        seedSummerVacationDiary();
    }

    /**
     * 데모용 대표 책 — 와이어프레임(T2목록/L2책상세/L3읽기 "촬영" 화면)에 나온 값 그대로.
     * totalPages/estimatedMinutes(12쪽/8분)는 T2·L2 화면에 "제대로 된 책"처럼 보이게 하려고
     * 원래 값을 그대로 유지한 표시용 메타데이터다 — 실제로 1~2, 4~12쪽 본문 데이터는 안 만든다.
     * 실제 문장 데이터가 있는 건 3쪽뿐이고, 그게 사용자가 준 문장 4개 그대로다(앞뒤 줄거리 지어내지 않음).
     * 이 4문장은 라이브 촬영 없이 데모를 진행하기 위해 DemoStuckSentenceSeeder가 전부 미리
     * "막힌 문장"으로 표시해둔다.
     */
    private void seedUglyDuckling() {
        Book book = bookRepository.save(new Book(
                "미운 아기 오리", "한스 크리스티안 안데르센", "/images/books/ugly-duckling/cover.png", "쉬움", 12, 8
        ));
        List<BookSentence> sentences = new ArrayList<>();
        addPage(sentences, book, 3, "/images/books/ugly-duckling/3.png",
                "형제들도, 다른 동물들도 그를 미워했어요.",
                "'넌 너무 못생겼어!'",
                "참다못한 아기 오리는 집을 떠났어요.",
                "외로운 오리는 깊은 숲 속으로 걸어갔답니다.");
        bookSentenceRepository.saveAll(sentences);
    }

    private void seedGiantTurnip() {
        Book book = bookRepository.save(new Book(
                "커다란 무", "옛이야기 모음", "/images/books/giant-turnip/cover.png", "보통", 3, 6
        ));
        List<BookSentence> sentences = new ArrayList<>();
        addPage(sentences, book, 1, "/images/books/giant-turnip/1.png",
                "할아버지는 밭에 커다란 무를 심었어요.",
                "무는 하루가 다르게 쑥쑥 자랐지요.");
        addPage(sentences, book, 2, "/images/books/giant-turnip/2.png",
                "할아버지 혼자서는 도저히 뽑을 수가 없었어요.",
                "할머니와 손자까지 나와서 힘껏 잡아당겼어요.");
        addPage(sentences, book, 3, "/images/books/giant-turnip/3.png",
                "마침내 커다란 무가 쑥 뽑혔어요.",
                "온 가족이 함께 웃으며 무를 안고 집으로 돌아갔어요.");
        bookSentenceRepository.saveAll(sentences);
    }

    private void seedBabyBearBath() {
        Book book = bookRepository.save(new Book(
                "아기 곰의 목욕", "숲속 이야기", "/images/books/baby-bear-bath/cover.png", "쉬움", 3, 4
        ));
        List<BookSentence> sentences = new ArrayList<>();
        addPage(sentences, book, 1, "/images/books/baby-bear-bath/1.png",
                "아기 곰은 목욕하는 걸 정말 싫어했어요.",
                "엄마 곰이 따뜻한 물을 받아 놓았어요.");
        addPage(sentences, book, 2, "/images/books/baby-bear-bath/2.png",
                "아기 곰은 살금살금 도망가려고 했지요.",
                "그런데 물속에 비눗방울이 둥둥 떠 있었어요.");
        addPage(sentences, book, 3, "/images/books/baby-bear-bath/3.png",
                "아기 곰은 비눗방울을 잡으려다 어느새 목욕을 다 끝냈어요.");
        bookSentenceRepository.saveAll(sentences);
    }

    private void seedSummerVacationDiary() {
        Book book = bookRepository.save(new Book(
                "여름 방학 일기", "우리 반 이야기", "/images/books/summer-vacation/cover.png", "어려움", 3, 7
        ));
        List<BookSentence> sentences = new ArrayList<>();
        addPage(sentences, book, 1, "/images/books/summer-vacation/1.png",
                "여름 방학이 시작되자 민준이는 시골 할머니 댁으로 떠났어요.",
                "넓은 들판에는 노란 참외가 주렁주렁 열려 있었어요.");
        addPage(sentences, book, 2, "/images/books/summer-vacation/2.png",
                "민준이는 할머니와 함께 옥수수를 땄어요.",
                "저녁에는 평상에 앉아 별을 세며 이야기를 나누었지요.");
        addPage(sentences, book, 3, "/images/books/summer-vacation/3.png",
                "방학이 끝날 무렵, 민준이는 할머니께 편지를 써 드렸어요.");
        bookSentenceRepository.saveAll(sentences);
    }

    private void addPage(List<BookSentence> target, Book book, int pageIndex, String pageImageUrl, String... texts) {
        for (int i = 0; i < texts.length; i++) {
            target.add(new BookSentence(book, pageIndex, i, texts[i], pageImageUrl));
        }
    }
}
