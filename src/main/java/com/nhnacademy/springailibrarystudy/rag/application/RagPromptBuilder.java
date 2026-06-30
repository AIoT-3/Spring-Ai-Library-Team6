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

            응답은 유효한 JSON 배열 하나만 반환하세요. 배열 앞뒤에 다른 텍스트를 붙이지 마세요.
            각 항목은 정확히 두 필드만 가집니다: id, recommendationReason
            id에는 후보 목록에 표시된 "id" 값(숫자)을 그대로 사용하세요. "도서 1" 같은 라벨이나 순번이 아니라 숫자 id여야 합니다.
            recommendationReason은 한국어 한 문장으로 작성하고, null이나 빈 값을 넣지 마세요.
            markdown, 코드블록, 주석은 포함하지 마세요.

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
