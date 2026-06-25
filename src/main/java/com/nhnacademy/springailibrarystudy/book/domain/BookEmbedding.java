package com.nhnacademy.springailibrarystudy.book.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.OffsetDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Table(
        name = "book_embeddings",
        indexes = {
                @Index(name = "idx_book_embeddings_book_id", columnList = "book_id"),
                @Index(name = "idx_book_embeddings_model", columnList = "embedding_model")
        },
        uniqueConstraints = @UniqueConstraint(
                name = "uk_book_embeddings_book_model_source",
                columnNames = {"book_id", "embedding_model", "source_text_hash"}
        )
)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BookEmbedding {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "book_embedding_sequence_generator"
    )
    @SequenceGenerator(
            name = "book_embedding_sequence_generator",
            sequenceName = "book_embedding_sequence",
            allocationSize = 1000
    )
    @ColumnDefault("nextval('book_embedding_sequence')")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "book_id",
            nullable = false,
            // Hibernate가 자동생성하는 이름은 추적이 어렵기 때문에 명시적으로 지정
            foreignKey = @ForeignKey(name = "fk_book_embeddings_book")
    )
    private Book book;

    // 임베딩 모델 이름
    @Column(name = "embedding_model", length = 100, nullable = false)
    private String embeddingModel;

    // source_text의 해시값: 임베딩에 사용된 컬럼들의 조합을 구분하기 위해 사용.
    @Column(name = "source_text_hash", length = 64, nullable = false)
    private String sourceTextHash;

    // 임베딩에 사용된 컬럼들을 조합한 텍스트.
    @Column(name = "source_text", columnDefinition = "TEXT", nullable = false)
    private String sourceText;

    // ddl-auto가 vector(1024) 컬럼을 만들도록 두는 읽기 전용 매핑.
    // 실제 저장과 검색은 BookEmbeddingRepository의 native SQL에서 처리하므로
    // JPA가 이 컬럼을 건드리지 않도록 insertable=false, updatable=false
    @Column(name = "embedding", columnDefinition = "vector(1024)",
            nullable = false, insertable = false, updatable = false)
    private String embedding;

    @ColumnDefault("current_timestamp")
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = OffsetDateTime.now();
    }
}
