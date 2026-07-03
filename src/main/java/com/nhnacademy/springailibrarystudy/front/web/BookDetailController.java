package com.nhnacademy.springailibrarystudy.front.web;

import com.nhnacademy.springailibrarystudy.book.application.BookDetailService;
import com.nhnacademy.springailibrarystudy.book.application.dto.BookDetailResponse;
import com.nhnacademy.springailibrarystudy.book.domain.Book;
import com.nhnacademy.springailibrarystudy.book.infrastructure.persistence.BookRepository;
import com.nhnacademy.springailibrarystudy.review.application.ReviewService;
import com.nhnacademy.springailibrarystudy.review.domain.BookReview;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequiredArgsConstructor
public class BookDetailController {

    private final BookDetailService bookDetailService;
    private final ReviewService reviewService;
    private final BookRepository bookRepository;

    /** 도서 상세 페이지 조회 */
    @GetMapping("/books/{bookId}")
    public String detail(@PathVariable Long bookId, Model model) {
        BookDetailResponse detail = bookDetailService.getDetail(bookId);
        model.addAttribute("book", detail);
        return "book/detail";
    }

    /** 리뷰 등록 */
    @PostMapping("/books/{bookId}/reviews")
    public String createReview(
            @PathVariable Long bookId,
            @RequestParam Integer rating,
            @RequestParam String content,
            @RequestParam String author,
            RedirectAttributes redirectAttributes
    ) {
        try {
            Book book = bookRepository.findById(bookId)
                    .orElseThrow(() -> new IllegalArgumentException("책을 찾을 수 없습니다. id=" + bookId));

            reviewService.createReview(BookReview.create(book, rating, content, author));

            redirectAttributes.addFlashAttribute("reviewMessage", "리뷰가 등록되었습니다.");
        } catch (Exception e) {
            log.error("리뷰 등록 실패: bookId={}", bookId, e);
            redirectAttributes.addFlashAttribute("reviewError", "리뷰 등록 중 오류가 발생했습니다.");
        }

        return "redirect:/books/" + bookId;
    }
}