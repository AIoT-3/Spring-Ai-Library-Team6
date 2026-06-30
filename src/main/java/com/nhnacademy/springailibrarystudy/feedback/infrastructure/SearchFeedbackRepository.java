package com.nhnacademy.springailibrarystudy.feedback.infrastructure;

import com.nhnacademy.springailibrarystudy.feedback.domain.SearchFeedback;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SearchFeedbackRepository extends JpaRepository<SearchFeedback, Long> {
}
