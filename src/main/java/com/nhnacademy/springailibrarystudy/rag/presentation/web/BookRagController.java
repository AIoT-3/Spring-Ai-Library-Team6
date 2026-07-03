package com.nhnacademy.springailibrarystudy.rag.presentation.web;

import com.nhnacademy.springailibrarystudy.rag.application.SearchBooksRagUseCase;
import com.nhnacademy.springailibrarystudy.rag.application.dto.GenerateRagAnswerCommand;
import com.nhnacademy.springailibrarystudy.rag.application.dto.GenerateRagAnswerResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class BookRagController {

    private final SearchBooksRagUseCase searchBooksRagUseCase;

    @GetMapping("/rag/recommend")
    public String recommendBooks(
            @RequestParam String question,
            Model model
    ) {
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
}
