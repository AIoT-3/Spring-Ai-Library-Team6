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

            [절대 규칙]
            1. 응답은 순수 JSON 배열만 반환하세요. 마크다운, 코드블록, 부가 텍스트는 금지입니다.
            2. 각 항목은 반드시 id 와 recommendationReason 두 필드만 가집니다. 다른 필드명은 절대 사용하지 마세요.
            3. id 는 후보 목록에 표시된 숫자를 따옴표 없이 정수로 그대로 사용하세요. 예시: 12345
            4. recommendationReason 은 한국어 한 문장으로 작성하고, 빈 값이나 null 금지.
            5. recommendationReason 값 내부에 큰따옴표와 작은따옴표를 사용하지 마세요.
            6. 책 제목을 언급할 때는 따옴표 없이 제목만 그대로 쓰세요.

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
