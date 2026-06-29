package com.nhnacademy.springailibrarystudy.rag.application;

import java.util.Map;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Component;

@Component
public class RagPromptBuilder {

    private static final String TEMPLATE = """
            당신은 도서 추천 도우미입니다.
            아래 후보 도서 목록에 있는 책만 사용하세요.
            후보 목록에 없는 책은 절대 추천하지 마세요.
            사용자 질문에 가장 적합한 도서를 최대 {recommendationTopK}권 고르세요.
            각 도서마다 추천 이유를 한국어 한 문장으로 작성하세요.

            응답은 JSON 배열만 반환하세요.
            각 항목은 id와 recommendationReason 필드를 가져야 합니다.
            설명 문장, markdown, 코드블록은 포함하지 마세요.

            [사용자 질문]
            {question}

            [후보 도서 목록]
            {context}
            """;

    public Prompt build(String question, String context, int recommendationTopK) {
        return new PromptTemplate(TEMPLATE).create(Map.of(
                "question", question,
                "context", context,
                "recommendationTopK", recommendationTopK
        ));
    }
}
