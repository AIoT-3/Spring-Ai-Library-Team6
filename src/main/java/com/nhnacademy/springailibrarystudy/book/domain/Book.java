package com.nhnacademy.springailibrarystudy.book.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Table(
        name = "books",
        uniqueConstraints = @UniqueConstraint(name = "uk_books_isbn13", columnNames = "isbn13")
)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Book {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "book_sequence_generator"
    )
    @SequenceGenerator(
            name = "book_sequence_generator",
            sequenceName = "book_sequence",
            allocationSize = 1
    )
    @ColumnDefault("nextval('book_sequence')")
    private Long id;

    @Column(name = "isbn13", length = 13) // 올바르지 않은 ISBN을 거르기 위해 null 허용
    private String isbn13;

    @Column(name = "volume_title", length = 50)
    private String volumeTitle;

    @Column(name = "title", length = 500, nullable = false)
    private String title;

    @Column(name = "author_name", length = 1000)
    private String authorName;

    @Column(name = "publisher_name", length = 255)
    private String publisherName;

    @Column(name = "published_date")
    private LocalDate publishedDate;

    @Column(name = "price", precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "kdc_code", length = 20)
    private String kdcCode;

    @ColumnDefault("current_timestamp")
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @ColumnDefault("current_timestamp")
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        this.isbn13 = IsbnNormalizer.normalizeToIsbn13(this.isbn13);
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.isbn13 = IsbnNormalizer.normalizeToIsbn13(this.isbn13);
        this.updatedAt = OffsetDateTime.now();
    }
}
