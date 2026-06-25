package com.nhnacademy.springailibrarystudy.search.presentation;

import com.nhnacademy.springailibrarystudy.search.application.SearchBooksUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class BookSearchController {

    private final SearchBooksUseCase searchBooksUseCase;


}
