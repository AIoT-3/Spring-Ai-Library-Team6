package com.nhnacademy.springailibrarystudy;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.ollama.OllamaContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.rabbitmq.RabbitMQContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

//    @Bean
//    @ServiceConnection
//    OllamaContainer ollamaContainer() {
//        return new OllamaContainer(DockerImageName.parse("ollama/ollama:latest"));
//    }

    @Bean
    @ServiceConnection
    PostgreSQLContainer pgvectorContainer() {
        return new PostgreSQLContainer(DockerImageName.parse("pgvector/pgvector:pg16"))
                .withDatabaseName("library_test")
                .withUsername("test")
                .withPassword("test");
    }

//    @Bean
//    @ServiceConnection
//    RabbitMQContainer rabbitContainer() {
//        return new RabbitMQContainer(DockerImageName.parse("rabbitmq:latest"));
//    }

//    @Bean
//    @ServiceConnection(name = "redis")
//    GenericContainer<?> redisContainer() {
//        return new GenericContainer<>(DockerImageName.parse("redis:latest")).withExposedPorts(6379);
//    }

}
