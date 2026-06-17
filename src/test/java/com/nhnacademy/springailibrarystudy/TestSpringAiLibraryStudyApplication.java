package com.nhnacademy.springailibrarystudy;

import org.springframework.boot.SpringApplication;

public class TestSpringAiLibraryStudyApplication {

    public static void main(String[] args) {
        SpringApplication
                .from(SpringAiLibraryStudyApplication::main)
                .with(TestcontainersConfiguration.class)
                .run(args);
    }

}
