package com.nhnacademy.springailibrarystudy.review.domain;

import com.nhnacademy.springailibrarystudy.book.domain.Book;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "book_reviews")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BookReview {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
            generator = "book_review_seq")
    @SequenceGenerator(name = "book_review_seq",
            sequenceName = "book_review_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(name = "rating", nullable = false)
    private Integer rating;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "author", nullable = false)
    private String author;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public static BookReview create(Book book, Integer rating,
                                    String content, String author) {
        BookReview r = new BookReview();
        r.book = book;
        r.rating = rating;
        r.content = content;
        r.author = author;
        return r;
    }
}