package com.nhnacademy.springailibrarystudy.rag.application;

import com.nhnacademy.springailibrarystudy.rag.application.dto.RagBookCandidate;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class RagContextBuilder {

    private static final int MAX_DESCRIPTION_LENGTH = 512;

    public String build(List<RagBookCandidate> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            return "";
        }

        StringBuilder context = new StringBuilder();
        for (int i = 0; i < candidates.size(); i++) {
            RagBookCandidate candidate = candidates.get(i);

            // DB에서 검색된 field만 컨텍스트에 넣음
            context.append("[도서 ").append(i + 1).append("]\n");
            appendLine(context, "title", candidate.title());
            appendLine(context, "author", candidate.authorName());
            appendLine(context, "publisher", candidate.publisherName());
            appendLine(context, "description", trimDescription(candidate.description()));
            appendLine(context, "similarity", candidate.similarity());
            context.append('\n');
        }

        return context.toString().trim();
    }

    private void appendLine(StringBuilder context, String label, Object value) {
        if (value == null) {
            return;
        }

        String text = value.toString().trim();
        if (StringUtils.hasText(text)) {
            context.append(label).append(": ").append(text).append('\n');
        }
    }

    private String trimDescription(String description) {
        if (!StringUtils.hasText(description) || description.length() <= MAX_DESCRIPTION_LENGTH) {
            return description;
        }

        return description.substring(0, MAX_DESCRIPTION_LENGTH).trim();
    }
}
