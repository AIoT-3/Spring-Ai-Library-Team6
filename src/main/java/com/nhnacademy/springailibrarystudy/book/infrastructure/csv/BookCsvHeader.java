package com.nhnacademy.springailibrarystudy.book.infrastructure.csv;

import java.util.Arrays;
import java.util.List;

public enum BookCsvHeader {
    SEQ_NO,
    ISBN_THIRTEEN_NO,
    VLM_NM,
    TITLE_NM,
    AUTHR_NM,
    PUBLISHER_NM,
    PBLICTE_DE,
    ADTION_SMBL_NM,
    PRC_VALUE,
    IMAGE_URL,
    BOOK_INTRCN_CN,
    KDC_NM,
    TITLE_SBST_NM,
    AUTHR_SBST_NM,
    TWO_PBLICTE_DE,
    INTNT_BOOKST_BOOK_EXST_AT,
    PORTAL_SITE_BOOK_EXST_AT,
    ISBN_NO;

    public static List<String> csvNames() {
        return Arrays.stream(values())
                .map(BookCsvHeader::name)
                .toList();
    }
}