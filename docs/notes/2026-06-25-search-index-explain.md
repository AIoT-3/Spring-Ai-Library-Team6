# 2026-06-25 Search Index Explain

- 원본 로그: `docs/logs/2026-06-25-search-index-explain.log`
- 실행 파일: `BookSearchIndexExplainTest.java`
- row 수: 157,118건
- 측정 방식: `EXPLAIN (ANALYZE, BUFFERS)`
- 총 실행 시간: 26분 29초
- 생성한 인덱스가 실제 실행 계획에서 선택됐는지 함께 확인함

## 실험 대상

- `isbn-exact`: `isbn13 = ?`
- `kdc-prefix`: `kdc_code like '500%'`
- `keyword`: `title`, `author_name`, `publisher_name` 대상 `%자바%` 검색
- `keyword-kdc`: keyword 검색 + `kdc_code like '500%'`
- `description-fts`: `description` 대상 `to_tsvector @@ plainto_tsquery`

각 시나리오는 목록 조회용 `content` 쿼리와 `Page` 전체 개수 계산용 `count` 쿼리를 분리함.

## 실행 시나리오

`content` 쿼리는 현재 검색 목록 응답에 필요한 컬럼만 조회함.

```sql
select id, volume_title, title, author_name, publisher_name, published_date, price, image_url
```

| scenario | 쿼리 형태 | 확인 목적 |
| --- | --- | --- |
| `isbn-exact-content` | `isbn13 = ? order by id limit 20` | ISBN 단건 조회의 목록 쿼리 계획 |
| `isbn-exact-count` | `count(*) where isbn13 = ?` | ISBN 조건의 count 비용 |
| `kdc-prefix-content` | `kdc_code like '500%' order by id limit 20` | KDC prefix 목록 쿼리 계획 |
| `kdc-prefix-count` | `count(*) where kdc_code like '500%'` | KDC prefix count 비용 |
| `keyword-content` | `lower(title/author_name/publisher_name) like '%자바%' order by id limit 20` | 현재 keyword 검색의 목록 쿼리 계획 |
| `keyword-count` | `count(*) where lower(title/author_name/publisher_name) like '%자바%'` | 현재 keyword 검색의 count 비용 |
| `keyword-kdc-content` | keyword 조건 + `kdc_code like '500%' order by id limit 20` | keyword와 KDC 필터 조합 |
| `keyword-kdc-count` | `count(*)` + keyword 조건 + `kdc_code like '500%'` | 조합 조건의 count 비용 |
| `description-fts-content` | `to_tsvector(...) @@ plainto_tsquery(...) order by id limit 20` | description 전문 검색 목록 쿼리 계획 |
| `description-fts-count` | `count(*) where to_tsvector(...) @@ plainto_tsquery(...)` | description 전문 검색 count 비용 |

## 인덱스 세트

| index set | 생성 인덱스 |
| --- | --- |
| `baseline` | 후보 인덱스 없음 |
| `kdc-btree` | `kdc_code` btree |
| `kdc-pattern` | `kdc_code varchar_pattern_ops` |
| `keyword-trgm` | `lower(title)`, `lower(author_name)`, `lower(publisher_name)` GIN trigram |
| `description-fts` | `to_tsvector('simple', coalesce(description, ''))` GIN |
| `combined-candidate` | `kdc-pattern`, `keyword-trgm`, `description-fts` 전체 |

## 결과 요약

| 시나리오 | baseline | 가장 유리한 결과 | 실행 계획 | 판단 |
| --- | ---: | ---: | --- | --- |
| `isbn-exact-content` | 2.388ms | 0.126ms | `uk_books_isbn13` | unique 제약 인덱스로 충분 |
| `isbn-exact-count` | 0.098ms | 0.056ms | `uk_books_isbn13` | 별도 인덱스 불필요 |
| `kdc-prefix-content` | 77.829ms | 1.025ms | `idx_books_kdc_code_pattern` | prefix 전용 인덱스 효과 큼 |
| `kdc-prefix-count` | 33.958ms | 0.050ms | `idx_books_kdc_code_pattern` | count 쿼리에서도 효과 큼 |
| `keyword-content` | 117.698ms | 66.297ms | `books_pkey` | trigram 인덱스가 선택되지 않음 |
| `keyword-count` | 132.675ms | 90.971ms | Parallel Seq Scan | trigram 인덱스 채택 근거 약함 |
| `keyword-kdc-content` | 19.244ms | 0.453ms | `idx_books_kdc_code_pattern` | KDC prefix로 후보를 줄인 뒤 keyword filter |
| `keyword-kdc-count` | 19.158ms | 0.384ms | `idx_books_kdc_code_pattern` | 복합 조건에서도 KDC 인덱스 효과 큼 |
| `description-fts-content` | 977.587ms | 0.269ms | `idx_books_description_fts` | FTS GIN 효과 매우 큼 |
| `description-fts-count` | 1105.755ms | 0.116ms | `idx_books_description_fts` | count 쿼리에서도 효과 매우 큼 |

## 해석

- `isbn13`: unique 제약 유지
  - 이미 `uk_books_isbn13` 사용
  - 성능 목적의 추가 인덱스 불필요

- `kdc_code`: `varchar_pattern_ops` 후보
  - `kdc_code like '500%'`에서 btree보다 명확하게 효과 확인
  - `content`: 77.829ms -> 1.025ms
  - `count`: 33.958ms -> 0.050ms

- `keyword`: trigram 인덱스 보류
  - `keyword-trgm`, `combined-candidate`에서도 `idx_books_title_trgm` 등은 선택되지 않음
  - `content` 쿼리는 `order by id limit 20` 영향으로 `books_pkey`를 사용
  - `count` 쿼리는 Parallel Seq Scan 유지
  - 현재 `%자바%` 단일 검색어 기준으로는 채택 근거 부족

- `keyword + kdc`: KDC 인덱스가 실질적인 최적화 포인트
  - KDC prefix로 79건까지 후보 축소
  - 이후 keyword 조건은 filter로 처리
  - 현재 검색 조건 조합에서는 keyword trigram보다 KDC prefix 인덱스가 더 효과적

- `description`: FTS GIN 후보
  - baseline은 Parallel Seq Scan으로 1초 내외
  - FTS GIN 적용 후 Bitmap Index Scan 사용
  - `content`: 977.587ms -> 0.269ms
  - `count`: 1105.755ms -> 0.116ms

## 선택 후보

```sql
CREATE INDEX IF NOT EXISTS idx_books_kdc_code_pattern
    ON books (kdc_code varchar_pattern_ops);

CREATE INDEX IF NOT EXISTS idx_books_description_fts
    ON books USING gin (to_tsvector('simple', coalesce(description, '')));
```

`title`, `author_name`, `publisher_name`의 trigram 인덱스는 이번 결과만으로는 보류.

## 한계

- 검색어는 `자바`, KDC prefix는 `500%`로 고정
- 검색어 빈도와 KDC prefix 선택도에 따른 추가 실험 필요
- 같은 DB에서 순차 실행했으므로 cache warm-up 영향 존재
- `content` 쿼리는 `order by id limit 20` 때문에 앞쪽 row에서 결과가 빨리 발견되면 유리하게 보일 수 있음
- PostgreSQL planner가 인덱스를 선택하지 않은 경우도 실험 결과로 기록

## 다음 작업

- `docs/sql/search-indexes.sql`에는 최종 선택 인덱스만 남기기
- keyword trigram은 검색어를 바꿔 추가 실험하거나, vector/hybrid search 이후 필요성 재검토
- `description` FTS는 keyword 검색과 분리된 full text search 기능으로 구현 후보
