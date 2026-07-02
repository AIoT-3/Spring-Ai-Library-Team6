package com.nhnacademy.springailibrarystudy.review.domain;

import com.nhnacademy.springailibrarystudy.book.domain.Book;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "book_review_summaries")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BookReviewSummary {

    @Id
    @Column(name = "book_id")
    private Long bookId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", insertable = false, updatable = false)
    private Book book;

    @Column(name = "review_count", nullable = false)
    private Long reviewCount = 0L;

    @Column(name = "average_rating", precision = 3, scale = 2)
    private BigDecimal averageRating = BigDecimal.ZERO;

    @Column(name = "rating_1_count", nullable = false)
    private Integer rating1Count = 0;
    @Column(name = "rating_2_count", nullable = false)
    private Integer rating2Count = 0;
    @Column(name = "rating_3_count", nullable = false)
    private Integer rating3Count = 0;
    @Column(name = "rating_4_count", nullable = false)
    private Integer rating4Count = 0;
    @Column(name = "rating_5_count", nullable = false)
    private Integer rating5Count = 0;

    @Column(name = "last_reviewed_at")
    private LocalDateTime lastReviewedAt;

    @Column(name = "review_summary", columnDefinition = "TEXT")
    private String reviewSummary;

    @Column(name = "is_summary_dirty", nullable = false)
    private Boolean isSummaryDirty = true;

    @Column(name = "last_summarized_count", nullable = false)
    private Long lastSummarizedCount = 0L;

    @Column(name = "is_generating", nullable = false)
    private Boolean isGenerating = false;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    @PrePersist
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }


    public static BookReviewSummary create(Long bookId) {
        BookReviewSummary r = new BookReviewSummary();
        r.bookId = bookId;
        return r;
    }


    public void updateStatistics(Long reviewCount, BigDecimal averageRating,
                                 Integer r1, Integer r2, Integer r3,
                                 Integer r4, Integer r5,
                                 LocalDateTime lastReviewedAt) {
        this.reviewCount = reviewCount;
        this.averageRating = averageRating;
        this.rating1Count = r1;
        this.rating2Count = r2;
        this.rating3Count = r3;
        this.rating4Count = r4;
        this.rating5Count = r5;
        this.lastReviewedAt = lastReviewedAt;
        this.isSummaryDirty = true;
    }

    public void markAsGenerating() {
        this.isGenerating = true;
    }

    public void finishGenerating() {
        this.isGenerating = false;
    }

    public void updateSummary(String newSummary, Long lastSummarizedReviewId) {
        this.reviewSummary = newSummary;
        this.lastSummarizedCount = lastSummarizedReviewId;
        this.isSummaryDirty = false;
        this.isGenerating = false;
    }

    public boolean shouldGenerateSummary() {
        return reviewCount >= 5
                && Boolean.TRUE.equals(isSummaryDirty)
                && Boolean.FALSE.equals(isGenerating);
    }
}