package com.nhnacademy.springailibrarystudy.review.infrastructure;

import com.nhnacademy.springailibrarystudy.review.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {
}
