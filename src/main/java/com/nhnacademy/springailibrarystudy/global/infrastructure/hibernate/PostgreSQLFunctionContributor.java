package com.nhnacademy.springailibrarystudy.global.infrastructure.hibernate;

import org.hibernate.boot.model.FunctionContributions;
import org.hibernate.boot.model.FunctionContributor;
import org.hibernate.type.StandardBasicTypes;

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
