package com.nhnacademy.springailibrarystudy.front.web;

import com.nhnacademy.springailibrarystudy.rag.application.SearchBooksRagUseCase;
import com.nhnacademy.springailibrarystudy.rag.application.dto.GenerateRagAnswerCommand;
import com.nhnacademy.springailibrarystudy.rag.application.dto.GenerateRagAnswerResult;
import com.nhnacademy.springailibrarystudy.rag.application.dto.RagBookCandidate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/rag")
public class RagAjaxController {

    private final SearchBooksRagUseCase searchBooksRagUseCase;

    @GetMapping("/async")
    public ResponseEntity<GenerateRagAnswerResult> generate(@RequestParam String question) {
        GenerateRagAnswerCommand command = GenerateRagAnswerCommand.of(question);

        // 후보 검색을 다시 수행 (캐시 히트 시 generateAnswer 내부에서 바로 반환됨)
        List<RagBookCandidate> candidates = searchBooksRagUseCase.searchCandidates(command);
        GenerateRagAnswerResult result = searchBooksRagUseCase.generateAnswer(command, candidates);

        return ResponseEntity.ok(result);
    }
}