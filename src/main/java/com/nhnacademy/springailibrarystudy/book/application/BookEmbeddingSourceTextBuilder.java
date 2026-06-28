package com.nhnacademy.springailibrarystudy.book.application;

import com.nhnacademy.springailibrarystudy.book.application.dto.BookEmbeddingTarget;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

@Service
public class BookEmbeddingSourceTextBuilder {

    private static final int MAX_SOURCE_TEXT_LENGTH = 4_000;
    private static final Pattern HTML_TAG = Pattern.compile("<[^>]*>");
    private static final Pattern WHITESPACE = Pattern.compile(" \\s+");

    public String build(BookEmbeddingTarget target) {
        List<String> lines = new ArrayList<>();
        addLine(lines, "제목", target.title());
//        addLine(lines, "권 정보", target.volumeTitle());
//        addLine(lines, "저자", target.authorName());
//        addLine(lines, "출판사", target.publisherName());
        addLine(lines, "소개", target.description());

        return trimToMaxLength(String.join("\n", lines));
    }

    public String hash(String sourceText) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(sourceText.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 알고리즘은 Java 표준 라이브러리에서 항상 지원되므로, 이 예외는 발생하지 않아야 함.
            // 그래서 BusinessException 대신 IllegalStateException을 던져서, 이 상태가 비정상적임을 나타냄.
            throw new IllegalStateException("SHA-256 algorithm이 지원되지 않는 상태입니다.", e);
        }
    }

    private void addLine(List<String> lines, String label, String value) {
        String normalized = normalize(value);
        if (!normalized.isBlank()) {
            lines.add(label + ": " + normalized);
        }
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }

        // HTML 엔티티를 디코딩하고, HTML 태그를 제거하고, 연속된 공백을 단일 공백으로 변환
        String decoded = value
                .replace("&nbsp;", " ")
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&#39;", "'");

        String withoutHtml = HTML_TAG.matcher(decoded).replaceAll("");

        return WHITESPACE.matcher(withoutHtml).replaceAll(" ").trim();
    }

    private String trimToMaxLength(String sourceText) {
        if (sourceText.length() <= MAX_SOURCE_TEXT_LENGTH) {
            return sourceText;
        }

        return sourceText.substring(0, MAX_SOURCE_TEXT_LENGTH).trim();
    }
}
