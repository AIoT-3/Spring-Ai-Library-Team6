# 2026-06-25 Search Target Profile

- 대상 파일: `src/main/resources/data/BOOK_DB_202112.csv`
- 실행 파일: `BookCsvSearchTargetProfileTest.java`
- row 수: 157,118건
- 전제: PostgreSQL 16, `pg_trgm`, `pgvector` 기준

## CSV 분석 결과

```
12:34:19.133 [main] INFO com.nhnacademy.springailibrarystudy.book.infrastructure.csv.BookCsvSearchTargetProfileTest -- 
                                                   Book CSV Search Target Length 분석 
      column       |  filled  |      avgLength       |  p50  |  p90  |  p95  |  p99  |  maxLength  |  unique  |      uniqueRate      |
--------------------------------------------------------------------------------------------------------------------------------------
         TITLE_NM  |  157118  |   26.07805598340101  |   20  |   51  |   70  |  116  |        313  |  120804  |   76.88743492152395  |
           VLM_NM  |   39708  |  2.2381635942379368  |    1  |    5  |    8  |   11  |         20  |    1738  |   4.376951747758638  |
         AUTHR_NM  |  157039  |  13.132107310922764  |   11  |   22  |   27  |   53  |        750  |   94882  |   60.41938626710562  |
     PUBLISHER_NM  |  151577  |   6.516034754613167  |    5  |   12  |   16  |   27  |        137  |   20981  |  13.841809773250558  |
   BOOK_INTRCN_CN  |   84205  |  137.47213348375988  |  141  |  177  |  192  |  277  |        384  |   74372  |   88.32254616709221  |
           KDC_NM  |  143057  |   5.129647622975457  |    5  |    7  |    8  |   10  |         14  |    9199  |   6.430304004697429  |
 ISBN_THIRTEEN_NO  |  157118  |                  13  |   13  |   13  |   13  |   13  |         13  |  157118  |                 100  |
```

- p50/p90/p95/p99: 각각 전체의 50%/90%/95%/99%에 해당하는 길이

## 결과 해석

- `TITLE_NM`: keyword 검색 1차 대상
  - 평균 26자, p95 70자, p99 116자
  - 부분 문자열 검색 성능 비교 우선순위 높음

- `AUTHR_NM`: keyword 검색 대상, 전처리 주의
  - p99 53자, max 750자
  - 저자명, 역할 표기, 여러 저자 구분자가 섞인 값이 많음
  - 표시용 원문과 임베딩용 전처리 텍스트 분리 후보

- `PUBLISHER_NM`: 검색/필터 후보, 데이터 품질 확인 필요
  - 짧고 반복 값이 많음
  - 같은 출판사가 동일 문자열로 들어가는지 확인 필요
  - exact filter는 정규화 가능성 확인 후 판단

- `BOOK_INTRCN_CN`: 전문 검색, 임베딩, RAG 후보
  - 평균 137자, p99 277자
  - 일반 keyword OR 조건에 섞기보다 별도 검색 대상으로 분리

- `VLM_NM`: 제목 보조 정보
  - filled 낮고 길이 짧음
  - 독립 검색 조건보다 제목 검색 보조와 목록 표시 용도

- `KDC_NM`: KDC code성 카테고리 필터 후보
  - 자연어 검색어보다 prefix 필터에 가까움
  - `500`, `510` 같은 상위 분류 검색에 적합

- `ISBN_THIRTEEN_NO`: 식별자 조회 대상
  - 부분 검색이나 전문 검색 대상 아님
  - unique exact lookup 대상

## 인덱스 종류

| 종류 | 특징 |
| --- | --- |
| btree | exact match, 범위 검색, 정렬에 가장 일반적으로 사용 |
| unique btree | 중복 방지와 exact lookup을 동시에 처리 |
| composite btree | 여러 조건을 함께 자주 사용할 때 후보 |
| partial index | 특정 조건의 row만 자주 조회할 때 후보 |
| expression index | `lower(column)`, `reverse(column)` 같은 계산 결과에 인덱스 |
| `varchar_pattern_ops` | `LIKE 'abc%'` 형태의 prefix 검색을 btree로 태우기 위한 opclass |
| GIN | 하나의 row가 여러 key/token을 가질 때 유리한 inverted index 계열 |
| `pg_trgm` GIN | 문자열을 trigram으로 쪼개 부분 검색, suffix 검색, 유사도 검색에 사용 |
| FTS GIN | `tsvector` 기반 전문 검색에 사용, 토큰화 품질 영향 큼 |
| BRIN | 큰 테이블에서 물리적 저장 순서와 값의 순서가 비슷할 때 후보 |
| vector index | 임베딩 벡터의 근접 검색에 사용 |

## 상황별 인덱스 전략

| 상황 | 쿼리 예시 | 일반적인 인덱스 후보 | 대상 컬럼 | 적용 판단 |
| --- | --- | --- | --- | --- |
| 고유 식별자 exact match | `isbn13 = ?` | unique btree | `isbn13` | unique 제약으로 충분, 별도 중복 index 불필요 |
| 일반 값 exact match | `publisher_name = ?` | btree | `publisherName` | 출판사 표기 흔들림 확인 후 필터화 판단 |
| 복합 조건 검색 | `publisher_name = ? and published_date >= ?` | composite btree |  | 현재 고정 복합 조건 없음 |
| 숫자/날짜 범위 검색 | `published_date >= ?` | btree |  | 현재 검색 조건 아님 |
| 정렬 기준 | `order by published_date` | btree |  | 현재 기본 정렬은 id 기준 |
| 문자열 prefix/suffix 검색 | `code like '500%'`, `title like '%자바'` | btree pattern ops, reverse expression index, `pg_trgm` GIN | `kdcCode`(prefix) | KDC prefix 검색은 적합, suffix 대상은 현재 없음 |
| 문자열 부분 검색 | `lower(title) like '%자바%'` | `lower(column)` 기준 `pg_trgm` GIN | `title`, `authorName`, `publisherName` | 현재 `containsIgnoreCase` 성능 비교 후보 |
| 문자열 유사도 검색 | `similarity(title, ?) > ?` | `pg_trgm` GIN | `title` | 오타/유사 검색 실험 후보 |
| 긴 텍스트 전문 검색 | `to_tsvector(...) @@ plainto_tsquery(...)` | FTS GIN | `description` | description 검색 실험 후보 |
| 낮은 선택도 조건 | `status = 'ACTIVE'` | partial index |  | 현재 상태 컬럼 없음 |
| JSON key/value 검색 | `metadata @> ...` | GIN |  | 현재 JSON 컬럼 없음 |
| 배열 포함 검색 | `tags @> ...` | GIN |  | 현재 배열 컬럼 없음 |
| 대용량 append 테이블 검색 | `created_at between ? and ?` | BRIN |  | 현재 주요 검색 조건 아님 |
| 의미 기반 검색 | `embedding <=> ?` | vector index | `book_embeddings.embedding` | step2 vector search에서 별도 실험 |

## 결론

- keyword 검색: `title`, `authorName`, `publisherName`은 QueryDSL `containsIgnoreCase` 유지
- title 보조 검색: `volumeTitle`은 keyword 검색에 포함하되 별도 인덱스 우선순위 낮음
- KDC prefix 검색: `kdcCode`는 btree/pattern ops 비교 후보
- full text search: `description`은 별도 실험 대상으로 분리
- 인덱스 적용 여부: `EXPLAIN ANALYZE` 비교 후 결정
  - index 없음
  - JPA 기본 index
  - `pg_trgm` GIN
  - FTS GIN
