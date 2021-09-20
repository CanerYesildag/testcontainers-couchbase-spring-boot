package com.caner.testcontainers.infrastructure.couchbase;

import com.couchbase.client.core.endpoint.CircuitBreakerConfig;
import com.couchbase.client.core.env.IoConfig;
import com.couchbase.client.core.env.TimeoutConfig;
import com.couchbase.client.core.retry.BestEffortRetryStrategy;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.ClusterOptions;
import com.couchbase.client.java.ReactiveCluster;
import com.couchbase.client.java.ReactiveCollection;
import com.couchbase.client.java.codec.JacksonJsonSerializer;
import com.couchbase.client.java.env.ClusterEnvironment;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@RequiredArgsConstructor
public class CouchbaseConfiguration {

    private final CouchbaseConfigProperty configProperty;

    @Bean
    public ClusterEnvironment clusterEnvironment(ObjectMapper objectMapper) {
        return ClusterEnvironment.builder()
                .jsonSerializer(JacksonJsonSerializer.create(objectMapper))
                .ioConfig(IoConfig.kvCircuitBreakerConfig(CircuitBreakerConfig.builder()
                        .enabled(false)))
                .timeoutConfig(TimeoutConfig
                        .kvTimeout(Duration.ofMillis(configProperty.getKvTimeoutMs()))
                        .queryTimeout(Duration.ofMillis(configProperty.getQueryTimeoutMs()))
                        .connectTimeout(Duration.ofMillis(configProperty.getConnectTimeoutMs())))
                .retryStrategy(BestEffortRetryStrategy.INSTANCE)
                .build();
    }

    @Bean
    public ReactiveCluster reactiveCluster(ClusterEnvironment clusterEnvironment) {
        final ReactiveCluster personReactiveCluster = Cluster.connect(configProperty.getBootstrapHost(),
                ClusterOptions.clusterOptions(configProperty.getUsername(), configProperty.getPassword())
                        .environment(clusterEnvironment)).reactive();
        personReactiveCluster.waitUntilReady(Duration.ofMillis(configProperty.getWaitUntilReadyMs()));
        return personReactiveCluster;
    }

    @Bean
    public ReactiveCollection personReactiveCollection(ReactiveCluster reactiveCluster) {
        return reactiveCluster.bucket(configProperty.getPersonBucketName()).defaultCollection();
    }
}
