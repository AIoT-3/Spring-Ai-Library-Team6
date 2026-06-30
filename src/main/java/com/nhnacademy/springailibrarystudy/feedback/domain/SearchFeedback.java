package com.nhnacademy.springailibrarystudy.feedback.domain;

import com.nhnacademy.springailibrarystudy.book.domain.Book;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "search_feedbacks",
        indexes = {
                @Index(name = "idx_search_feedbacks_user_key", columnList = "user_key"),
                @Index(name = "idx_search_feedbacks_book_id", columnList = "book_id"),
                @Index(name = "idx_search_feedbacks_query_book", columnList = "query,book_id"),
                @Index(name = "idx_search_feedbacks_created_at", columnList = "created_at")
        }
)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SearchFeedback {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "search_feedback_sequence_generator"
    )
    @SequenceGenerator(
            name = "search_feedback_sequence_generator",
            sequenceName = "search_feedback_sequence",
            allocationSize = 1
    )
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "book_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_search_feedbacks_book_id")
    )
    private Book book;
    
    @Column(name = "user_key", length = 100, nullable = false)
    private String userKey;

    @Column(name = "query", length = 500, nullable = false)
    private String query;

    @Enumerated(EnumType.STRING)
    @Column(name = "feedback_type", length = 30, nullable = false)
    private FeedbackType feedbackType;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = OffsetDateTime.now();
    }
}
