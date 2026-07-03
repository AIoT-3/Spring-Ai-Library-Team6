package com.nhnacademy.springailibrarystudy.rag.application;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RagPromptBuilder {

    private static final String TEMPLATE = """
            당신은 도서 추천 도우미입니다.
            아래 후보 도서 목록에 있는 책만 사용하세요. 후보 목록에 없는 책은 절대 언급하지 마세요.

            후보 도서 목록에 있는 모든 도서를 순서대로 검토하여, 사용자 질문에 실제로 적합한지 판단하세요.
            적합하다고 판단한 도서는 recommended 를 true 로, 적합하지 않다고 판단한 도서는 false 로 표시하세요.
            recommended 가 true 인 도서는 최대 {recommendationTopK}권까지만 표시하세요.
            적합한 도서가 {recommendationTopK}권보다 적어도 괜찮습니다. 개수를 억지로 맞추지 마세요.
            각 도서마다 판단 이유를 한국어 한 문장으로 작성하세요.

            [절대 규칙]
            1. 응답은 순수 JSON 배열만 반환하세요. 마크다운, 코드블록, 부가 텍스트는 금지입니다.
            2. 각 항목은 반드시 id, recommended, recommendationReason 세 필드만 가집니다. 다른 필드명은 절대 사용하지 마세요.
            3. id 는 각 후보 도서 블록의 'id:' 필드에 적힌 값을 따옴표 없이 정수로 그대로 사용하세요. 예시: id: 50104 -> 50104
            4. recommended 는 true 또는 false 불리언 값만 사용하세요. 따옴표로 감싸지 마세요.
            5. recommendationReason 은 한국어 한 문장으로 작성하고, 빈 값이나 null 금지.
            6. recommendationReason 값 내부에 큰따옴표와 작은따옴표를 사용하지 마세요.
            7. 책 제목을 언급할 때는 따옴표 없이 제목만 그대로 쓰세요.

            [사용자 질문]
            {question}

            [후보 도서 목록]
            {context}
            """;

    private final ChatOptions jsonResponseOptions;

    public RagPromptBuilder(ChatModel chatModel) {
        // num-ctx, num-predict, think 등 application.yaml 기본값을 유지한 채 format만 json으로 덮어씀
        OllamaChatOptions defaultOptions = ((OllamaChatModel) chatModel).getOptions();
        this.jsonResponseOptions = defaultOptions.mutate()
                .format("json")
                .build();

        log.info("RAG ChatOptions 확인: model={}, numCtx={}, numPredict={}, think={}, format={}",
                defaultOptions.getModel(), defaultOptions.getNumCtx(), defaultOptions.getNumPredict(),
                defaultOptions.getThinkOption(), ((OllamaChatOptions) jsonResponseOptions).getFormat());
    }

    public Prompt build(String question, String context, int recommendationTopK) {
        return new PromptTemplate(TEMPLATE).create(Map.of(
                "question", question,
                "context", context,
                "recommendationTopK", recommendationTopK
        ), jsonResponseOptions);
    }
}
