# 2026-06-24 Batch Insert Comparison

대상 파일: `src/main/resources/data/BOOK_DB_202112.csv`
실행 파일: `BookBulkInsertPerformanceTest.java`
처리 row 수: 157,118건

## CSV 읽기 + CSV row 변환 + DB 적재: end-to-end 시간 측정 결과

환경:
- Java: Temurin 21.0.8
- Spring Boot: 4.1.0
- Spring AI: 2.0.0
- Maven Wrapper: Apache Maven 3.9.16
- DB: Docker `pgvector/pgvector:pg16`
- DB extensions: `vector`, `pg_trgm`
- JDBC URL: `jdbc:postgresql://localhost:5432/library?reWriteBatchedInserts=true`
- Hibernate: `ddl-auto=create`, `hibernate.jdbc.batch_size=1000`, `hibernate.order_inserts=true`
- 테스트 프로필: `bulk-test`
- 측정 범위: CSV 읽기, CSV row 변환, DB 적재를 포함한 end-to-end 시간
- 공정성 조건: 각 방식 실행 전 `book_embeddings`, `books`를 truncate하고 sequence를 1로 초기화

### 1회 측정
```
2026-06-24T13:45:08.500+09:00  INFO 7319 --- [spring-ai-library-study] [           main] .n.s.b.i.b.BookBulkInsertPerformanceTest : 
                                   Book Bulk Insert Performance                                   
    method      |  batchSize  |  rowLimit  |  rowCount  |  elapsedMillis  |    rowsPerSecond     |
--------------------------------------------------------------------------------------------------
   CopyManager  |       1000  |    157118  |    157118  |           1254  |  125293.46092503988  |
 EntityManager  |       1000  |    157118  |    157118  |           4408  |   35643.82940108893  |
  JdbcTemplate  |       1000  |    157118  |    157118  |           2037  |   77132.05694648993  |
```

### 2회 측정
```
2026-06-24T13:45:39.591+09:00  INFO 7471 --- [spring-ai-library-study] [           main] .n.s.b.i.b.BookBulkInsertPerformanceTest : 
                                   Book Bulk Insert Performance                                   
    method      |  batchSize  |  rowLimit  |  rowCount  |  elapsedMillis  |    rowsPerSecond     |
--------------------------------------------------------------------------------------------------
   CopyManager  |       1000  |    157118  |    157118  |           1499  |  104815.21014009339  |
 EntityManager  |       1000  |    157118  |    157118  |           4367  |    35978.4749255782  |
  JdbcTemplate  |       1000  |    157118  |    157118  |           2287  |   68700.48097944906  |
```

### 3회 측정
```
2026-06-24T13:46:05.640+09:00  INFO 7605 --- [spring-ai-library-study] [           main] .n.s.b.i.b.BookBulkInsertPerformanceTest : 
                                   Book Bulk Insert Performance                                   
    method      |  batchSize  |  rowLimit  |  rowCount  |  elapsedMillis  |    rowsPerSecond     |
--------------------------------------------------------------------------------------------------
   CopyManager  |       1000  |    157118  |    157118  |           1319  |  119119.02956785444  |
 EntityManager  |       1000  |    157118  |    157118  |           4327  |   36311.07002542177  |
  JdbcTemplate  |       1000  |    157118  |    157118  |           2346  |   66972.71952259165  |
```

## 해석과 한계

### 해석

- `CopyManager`는 PostgreSQL의 `COPY FROM STDIN` 경로를 사용해 가장 유리했음.
- `JdbcTemplate`은 `reWriteBatchedInserts=true` 설정 덕분에 일반 batch insert보다 효율적으로 동작함.
- `EntityManager`는 엔티티 생성, 영속성 컨텍스트 관리, lifecycle callback, flush/clear 비용이 포함되어 가장 느렸음.

### 한계

- 단일 로컬 머신에서 짧은 시간 동안 반복 측정했으므로 절대 성능 수치로 일반화하기 어려움.
- JVM warm-up, OS cache, Docker resource 상태에 따라 실행 시간이 달라질 수 있음.
- row 수가 약 15.7만 건으로, 수백만 건 이상에서의 병목은 별도 실험 필요.
- `CopyManager`의 `batchSize` 값은 표시 목적에 가깝고, 현재 구현에서는 하나의 COPY stream으로 적재.

## 결론

- 이번 데이터 크기와 로컬 환경에서는 `CopyManager > JdbcTemplate > EntityManager` 순서로 빨랐음. 
- 다만 구현 복잡도와 유지보수성을 함께 고려하면 일반적인 애플리케이션 로직에서는 `JdbcTemplate` batch insert도 충분히 실용적이고, 초기 대량 적재나 마이그레이션성 작업에서는 `CopyManager`가 가장 적합함.
