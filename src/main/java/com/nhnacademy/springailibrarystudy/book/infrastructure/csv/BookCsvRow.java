package com.nhnacademy.springailibrarystudy.book.infrastructure.csv;

import com.nhnacademy.springailibrarystudy.global.exception.BusinessException;
import com.nhnacademy.springailibrarystudy.global.exception.ErrorCode;
import java.util.List;
import org.apache.commons.csv.CSVRecord;

public record BookCsvRow(
        String sourceSeqNo,
        String isbn13,
        String volumeTitle,
        String title,
        String authorName,
        String publisherName,
        String firstPublishedDate,
        String additionSymbol,
        String price,
        String imageUrl,
        String description,
        String kdcCode,
        String titleSearchText,
        String authorSearchText,
        String publishedDate,
        String internetBookstoreExists,
        String portalSiteBookExists,
        String isbnNo
) {

    public static void validateHeaders(List<String> headers) {
        if (!BookCsvHeader.csvNames().equals(headers)) {
            throw new BusinessException(ErrorCode.INVALID_BOOK_CSV);
        }
    }

    public static BookCsvRow from(CSVRecord csvRecord) {
        if (csvRecord.size() != BookCsvHeader.values().length) {
            throw new BusinessException(ErrorCode.INVALID_BOOK_CSV);
        }

        return new BookCsvRow(
                csvRecord.get(BookCsvHeader.SEQ_NO.ordinal()),
                csvRecord.get(BookCsvHeader.ISBN_THIRTEEN_NO.ordinal()),
                csvRecord.get(BookCsvHeader.VLM_NM.ordinal()),
                csvRecord.get(BookCsvHeader.TITLE_NM.ordinal()),
                csvRecord.get(BookCsvHeader.AUTHR_NM.ordinal()),
                csvRecord.get(BookCsvHeader.PUBLISHER_NM.ordinal()),
                csvRecord.get(BookCsvHeader.PBLICTE_DE.ordinal()),
                csvRecord.get(BookCsvHeader.ADTION_SMBL_NM.ordinal()),
                csvRecord.get(BookCsvHeader.PRC_VALUE.ordinal()),
                csvRecord.get(BookCsvHeader.IMAGE_URL.ordinal()),
                csvRecord.get(BookCsvHeader.BOOK_INTRCN_CN.ordinal()),
                csvRecord.get(BookCsvHeader.KDC_NM.ordinal()),
                csvRecord.get(BookCsvHeader.TITLE_SBST_NM.ordinal()),
                csvRecord.get(BookCsvHeader.AUTHR_SBST_NM.ordinal()),
                csvRecord.get(BookCsvHeader.TWO_PBLICTE_DE.ordinal()),
                csvRecord.get(BookCsvHeader.INTNT_BOOKST_BOOK_EXST_AT.ordinal()),
                csvRecord.get(BookCsvHeader.PORTAL_SITE_BOOK_EXST_AT.ordinal()),
                csvRecord.get(BookCsvHeader.ISBN_NO.ordinal())
        );
    }
}
