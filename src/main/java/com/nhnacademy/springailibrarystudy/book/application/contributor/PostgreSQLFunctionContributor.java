package com.nhnacademy.springailibrarystudy.book.application.contributor;

import org.hibernate.boot.model.FunctionContributions;
import org.hibernate.boot.model.FunctionContributor;
import org.hibernate.type.StandardBasicTypes;

// FIXME: Book의 application 로직이 아니라 Hibernate에 PostgreSQL의 전용 함수를 등록하는 역할이라 위치 이동 필요
// book.infrastructure.persistence도 괜찮지만, 다른 도메인에서도 쓸 가능성이 있어서
// global.infrastructure.hibernate 같은 패키지로 이동하는 것 고려
public class PostgreSQLFunctionContributor implements FunctionContributor {
    @Override
    public void contributeFunctions(FunctionContributions functionContributions) {
        functionContributions.getFunctionRegistry()
                .registerPattern(
                        "ts_match_korean",
                        "to_tsvector('korean', ?1) @@ plainto_tsquery('korean', ?2)",
                        functionContributions.getTypeConfiguration()
                                .getBasicTypeRegistry()
                                .resolve(StandardBasicTypes.BOOLEAN)
                );
        functionContributions.getFunctionRegistry()
                .registerPattern(
                        "vector_cosine_similarity",
                        "(1.0 - (embedding <=> cast(?1 as vector)))",
                        functionContributions.getTypeConfiguration()
                                .getBasicTypeRegistry()
                                .resolve(StandardBasicTypes.DOUBLE)
                );
    }
}
