package com.nhnacademy.springailibrarystudy.front.web;

import com.nhnacademy.springailibrarystudy.rag.application.SearchBooksRagUseCase;
import com.nhnacademy.springailibrarystudy.rag.application.dto.GenerateRagAnswerCommand;
import com.nhnacademy.springailibrarystudy.rag.application.dto.GenerateRagAnswerResult;
import com.nhnacademy.springailibrarystudy.search.application.SearchBooksUseCase;
import com.nhnacademy.springailibrarystudy.search.domain.SearchType;
import com.nhnacademy.springailibrarystudy.search.presentation.dto.BookSearchItemResponse;
import com.nhnacademy.springailibrarystudy.search.presentation.dto.BookSearchRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/")
@Controller
public class WebController {

    private final SearchBooksUseCase searchBooksUseCase;
    private final SearchBooksRagUseCase searchBooksRagUseCase;
    @GetMapping
    public String index(@ModelAttribute BookSearchRequest request,
                        BindingResult bindingResult,
                        Pageable pageable,
                        Model model) {
        log.info("GET / request: {}", request);

        if (bindingResult.hasErrors()) {
            model.addAttribute("books", List.of());
            model.addAttribute("page", null);
            model.addAttribute("request", request);

            return "index/index";
        }

        String question = request.query();

        if (request.searchType() == SearchType.RAG) {
            try {
                GenerateRagAnswerResult result = searchBooksRagUseCase.answer(
                        GenerateRagAnswerCommand.of(question)
                );

                model.addAttribute("question", question);
                model.addAttribute("answer", result.answer());
                model.addAttribute("books", result.books());
                model.addAttribute("fallback", result.fallback());

                return "rag/result";
            } catch (Exception e) {
                model.addAttribute("question", question);
                model.addAttribute("error", "추천 중 오류가 발생했습니다.");
                model.addAttribute("errorMessage", e.getMessage());
                return "rag/error";
            }
        }

        // 하이브리드 검색 처리 (일반 검색)
        long startTime = System.currentTimeMillis();

        // 검색 수행
        Page<BookSearchItemResponse> results = searchBooksUseCase.search(request, pageable);

        long endTime = System.currentTimeMillis();
        double searchTime = (endTime - startTime) / 1000.0;

        // 모델 데이터 주입
        model.addAttribute("request", request);
        model.addAttribute("books", results.getContent());
        model.addAttribute("page", results);
        model.addAttribute("searchTime", searchTime);

        return "index/index";
    }

}
