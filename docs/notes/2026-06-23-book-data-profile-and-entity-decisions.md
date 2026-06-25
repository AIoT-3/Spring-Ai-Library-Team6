# 2026-06-23 Book Data Profile And Entity Decisions

대상 파일: `src/main/resources/data/BOOK_DB_202112.csv`
실행 파일: `BookCsvProfileTest.java`
row 수: 157,118건

## CSV 분석 결과

```
2026-06-24T13:58:09.315+09:00  INFO 22982 --- [spring-ai-library-study] [           main] c.n.s.b.i.csv.BookCsvProfileTest         : 
              Book CSV 요약               
    metric      |        value         |
----------------------------------------
          file  |  BOOK_DB_202112.csv  |
          rows  |              157118  |
       columns  |                  18  |
 headerMatches  |                true  |

2026-06-24T13:58:09.623+09:00  INFO 22982 --- [spring-ai-library-study] [           main] c.n.s.b.i.csv.BookCsvProfileTest         : 
                                         Book CSV Column 분석                                         
          column            |  filled  |  missing  |       fillRate       |  unique  |  maxLength  |
----------------------------------------------------------------------------------------------------
                    SEQ_NO  |  157118  |        0  |                 100  |  157118  |          7  |
          ISBN_THIRTEEN_NO  |  157118  |        0  |                 100  |  157118  |         13  |
                    VLM_NM  |   39708  |   117410  |   25.27272495831159  |    1738  |         20  |
                  TITLE_NM  |  157118  |        0  |                 100  |  120804  |        313  |
                  AUTHR_NM  |  157039  |       79  |   99.94971931923777  |   94882  |        750  |
              PUBLISHER_NM  |  151577  |     5541  |   96.47335123919602  |   20981  |        137  |
                PBLICTE_DE  |       0  |   157118  |                   0  |       0  |          0  |
            ADTION_SMBL_NM  |  129589  |    27529  |   82.47877391514658  |    2123  |          5  |
                 PRC_VALUE  |  111394  |    45724  |   70.89830573199761  |    4342  |          9  |
                 IMAGE_URL  |   98337  |    58781  |    62.5879911913339  |   98331  |        134  |
            BOOK_INTRCN_CN  |   84205  |    72913  |  53.593477513715804  |   74372  |        384  |
                    KDC_NM  |  143057  |    14061  |   91.05067528863657  |    9199  |         14  |
             TITLE_SBST_NM  |  148739  |     8379  |   94.66706551763643  |  110233  |        254  |
             AUTHR_SBST_NM  |  149217  |     7901  |   94.97129545946359  |   85851  |        593  |
            TWO_PBLICTE_DE  |  103518  |    53600  |   65.88551279929735  |    9885  |         10  |
 INTNT_BOOKST_BOOK_EXST_AT  |  154554  |     2564  |    98.3681055003246  |       1  |          1  |
  PORTAL_SITE_BOOK_EXST_AT  |  154554  |     2564  |    98.3681055003246  |       1  |          1  |
                   ISBN_NO  |  100516  |    56602  |   63.97484693033262  |  100516  |         41  |

2026-06-24T13:58:09.833+09:00  INFO 22982 --- [spring-ai-library-study] [           main] c.n.s.b.i.csv.BookCsvProfileTest         : 
                    Book CSV ISBN 분석                     
 isbn13   |  isbn10   |  count   |         rate         |
---------------------------------------------------------
   valid  |    valid  |   24068  |  15.318423096017007  |
   valid  |  invalid  |  132192  |   84.13549052304637  |
 invalid  |    valid  |       0  |                   0  |
 invalid  |  invalid  |     858  |  0.5460863809366209  |

2026-06-24T13:58:09.877+09:00  INFO 22982 --- [spring-ai-library-study] [           main] c.n.s.b.i.csv.BookCsvProfileTest         : 
                Book CSV Price 분석                 
   status    |  count   |          rate          |
--------------------------------------------------
      blank  |   45724  |    29.101694268002394  |
    integer  |  111330  |     70.85757201593707  |
 nonInteger  |      64  |  0.040733716060540485  |

2026-06-24T13:58:09.905+09:00  INFO 22982 --- [spring-ai-library-study] [           main] c.n.s.b.i.csv.BookCsvProfileTest         : 
Book CSV Non-Integer Price Rows
  seqNo   |    price    |
-------------------------
 6356278  |   10590.00  |
 6356289  |   10410.00  |
 6356290  |   10470.00  |
 6358071  |   12220.00  |
 6362060  |   10600.00  |
 6362465  |   17420.00  |
  146131  |       3.95  |
  867429  |    3800.00  |
 6363294  |   18980.00  |
 1062410  |    8000.00  |
 1064463  |   58000.00  |
 1116240  |       3.99  |
 1116250  |       3.99  |
 1163573  |   15000.00  |
 1181423  |      25.50  |
 1414293  |       8.95  |
 1788617  |      15.00  |
 1232072  |   10000.00  |
 2660058  |   10000.00  |
 2039612  |   26000.00  |
 2039644  |   28000.00  |
 2042766  |   32000.00  |
 5119964  |   25000.00  |
 2090898  |   22000.00  |
 2323761  |   49370.00  |
 2325210  |   45000.00  |
 2325211  |   32000.00  |
 2340357  |   80340.00  |
 2644870  |   10950.00  |
 2644879  |   35670.00  |
 3356116  |   19190.00  |
 2777558  |   15000.00  |
 2777559  |   15000.00  |
 2777561  |   15000.00  |
 3572576  |   18540.00  |
 2911850  |   43190.00  |
 3580806  |   14750.00  |
 3596866  |  241020.00  |
 2650371  |   15000.00  |
 2777170  |   15000.00  |
 2777189  |   15000.00  |
 2777184  |   15000.00  |
 2777168  |   15000.00  |
 2777163  |   15000.00  |
 2777179  |   15000.00  |
 1119278  |   46000.00  |
 2777181  |   15000.00  |
 2777176  |   15000.00  |
 2777171  |   15000.00  |
 2777172  |   15000.00  |
 2777166  |   15000.00  |
 2777161  |   15000.00  |
 2777164  |   15000.00  |
 2777182  |   15000.00  |
 2777158  |   15000.00  |
 2777183  |   15000.00  |
 2777180  |   15000.00  |
 2777162  |   15000.00  |
 2777173  |   15000.00  |
 6365639  |   21210.00  |
 6365832  |   35430.00  |
 6365878  |   10410.00  |
 6367781  |   23640.00  |
 6370530  |   14760.00  |
```

## Book Entity Shape

| 필드 | 결정 | 이유                                                      |
| --- | --- |---------------------------------------------------------|
| `id` | sequence, allocationSize 1000 | JPA batch insert 기준으로 유리함. JDBC/COPY 실험에서는 별도 id 전략 고려. |
| `isbn13` | `varchar(13)`, unique | ISBN-13을 unique로 사용하되, 데이터에 잘못된 isbn때문에 nullable하게 둠.   |
| `volumeTitle` | `varchar(50)` | 권차/권호 정보. CSV max length 20이라 50이면 충분.                  |
| `title` | `varchar(500)`, required | 제목 검색/표시 핵심 필드.                                         |
| `authorName` | `varchar(1000)` | 저자 문자열은 정규화하지 않고 원문 보존.                                 |
| `publisherName` | `varchar(255)` | 출판사 문자열은 정규화하지 않고 원문 보존.                                |
| `publishedDate` | `LocalDate` | 값이 있는 `TWO_PBLICTE_DE`를 사용.                             |
| `price` | `BigDecimal(10, 2)` | 가격. 소수점 2자리까지 있는 경우가 존재해 BigDecimal 사용.                 |
| `imageUrl` | `TEXT` | URL 길이를 고정 길이로 빡빡하게 제한하지 않음.                            |
| `description` | `TEXT` | 책 소개. 검색/RAG context 후보.                                |
| `kdcCode` | `varchar(20)` | KDC는 분류 코드 성격의 문자열로 보관.                                 |
| `createdAt`, `updatedAt` | `OffsetDateTime` | 저장/수정 시각 추적.                                            |

## Excluded

- `SEQ_NO(sourceSeqNo)`: 원본 row 순번이라 Book 도메인 id로 쓰지 않음.
- `ISBN_NO(isbn10)`: 저장하지 않음. 입력/검색 시 ISBN-13으로 변환.
- `PBLICTE_DE(firstPublishedDate)`: `PBLICTE_DE`가 비어 있어 제외.
- `ADTION_SMBL_NM(adtionSymbol)`: ISBN 부가기호 성격이라 현재 검색 모델에서 제외.
- `TITLE_SBST_NM(titleSubtitle)`: 실제 subtitle이 아니라 검색용 정규화 파생값으로 보고 제외.
- `AUTHR_SBST_NM(authorSubtitle)`: 실제 subtitle이 아니라 검색용 정규화 파생값으로 보고 제외.
- 포털/인터넷서점 노출 여부: 현재 검색/표시 핵심 필드가 아니라 제외.

## Rules

- raw CSV DTO와 Book 엔티티는 분리해 사용.
- ISBN은 저장 전에 ISBN-13으로 정규화. 변환/검증 실패 시 `null`.
- CSV 프로파일링에서는 모든 컬럼을 문자열로 읽고, 날짜/가격/ISBN 변환 가능 여부는 별도 검사로 측정.

## Java Profile Code

- `BookCsvProfileTest`에서 Tablesaw로 CSV를 읽고 summary/column profile을 로그로 확인.
- `-Dcsv.profile=true`를 줄 때만 실행되게 해서 일반 테스트 흐름과 분리.
- `BookCsvParser`는 실제 import 실험용으로 row 단위 순회 구조 유지.
