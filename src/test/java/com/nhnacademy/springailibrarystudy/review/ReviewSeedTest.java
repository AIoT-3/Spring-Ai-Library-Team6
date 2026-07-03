package com.nhnacademy.springailibrarystudy.review;

import com.nhnacademy.springailibrarystudy.book.domain.Book;
import com.nhnacademy.springailibrarystudy.book.infrastructure.persistence.BookRepository;
import com.nhnacademy.springailibrarystudy.review.application.ReviewService;
import com.nhnacademy.springailibrarystudy.review.domain.BookReview;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class ReviewSeedTest {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private BookRepository bookRepository;

    private static final String ISBN_COMPUTER_SCIENCE_INTRO = "9788983250155"; // 컴퓨터과학개론
    private static final String ISBN_COMPUTER_INTRO = "9788970003481";         // 컴퓨터 개론

    @Test
    void seedReviewsForTwoBooks() throws InterruptedException {
        seedComputerScienceIntro();
        seedComputerIntro();

        // 마지막 리뷰 등록 이후, Dedup 윈도우(5초)가 완전히 풀리고
        // 큐 처리 + AI 요약(LLM 호출)까지 끝날 시간을 넉넉히 대기
        System.out.println("AI 요약 처리 대기 중...");
        Thread.sleep(60_000);
    }

    private void seedComputerScienceIntro() throws InterruptedException {
        Book book = findBook(ISBN_COMPUTER_SCIENCE_INTRO);

        List<BookReview> reviews = List.of(
                BookReview.create(book, 5, "컴퓨터공학 전공 필수 개념이 체계적으로 정리돼 있어요. 커리큘럼 순서대로 따라가기 좋습니다.", "user1"),
                BookReview.create(book, 4, "운영체제, 네트워크, 자료구조까지 폭넓게 다뤄서 전공 입문서로 딱입니다.", "user2"),
                BookReview.create(book, 5, "설명이 친절하고 그림 자료가 많아서 독학하기에도 무리 없었어요.", "user3"),
                BookReview.create(book, 3, "내용은 알찬데 분량이 많아서 완독하는 데 시간이 좀 걸렸습니다.", "user4"),
                BookReview.create(book, 5, "전공 시험 대비용으로 처음부터 끝까지 정독했는데 큰 도움이 됐습니다.", "user5")
        );

        for (BookReview review : reviews) {
            reviewService.createReview(review);
            Thread.sleep(5_500); // Dedup 윈도우(5초)보다 길게 대기해, 매 리뷰마다 큐에 확실히 적재되게 함
        }

        System.out.println("[컴퓨터과학개론] 리뷰 " + reviews.size() + "개 등록 완료 (bookId=" + book.getId() + ")");
    }

    private void seedComputerIntro() throws InterruptedException {
        Book book = findBook(ISBN_COMPUTER_INTRO);

        List<BookReview> reviews = List.of(
                BookReview.create(book, 4, "비전공자가 컴퓨터 기초를 익히기에 좋은 입문서입니다.", "user6"),
                BookReview.create(book, 5, "용어 설명이 쉬워서 IT 처음 접하는 분들께 추천합니다.", "user7"),
                BookReview.create(book, 3, "쉬운 편이라 전공자에게는 다소 기초적일 수 있어요.", "user8"),
                BookReview.create(book, 4, "하드웨어/소프트웨어 개념을 균형 있게 다뤄서 개론서로 무난합니다.", "user9"),
                BookReview.create(book, 5, "실습 없이 이론만으로도 컴퓨터 전반을 이해하는 데 충분했습니다.", "user10")
        );

        for (BookReview review : reviews) {
            reviewService.createReview(review);
            Thread.sleep(5_500); // Dedup 윈도우(5초)보다 길게
        }

        System.out.println("[컴퓨터 개론] 리뷰 " + reviews.size() + "개 등록 완료 (bookId=" + book.getId() + ")");
    }

    private Book findBook(String isbn13) {
        return bookRepository.findByIsbn13(isbn13)
                .orElseThrow(() -> new IllegalStateException("책을 찾을 수 없습니다. isbn13=" + isbn13));
    }
}