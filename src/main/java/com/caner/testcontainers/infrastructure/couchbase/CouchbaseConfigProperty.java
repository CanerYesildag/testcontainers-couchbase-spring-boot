package com.caner.testcontainers.infrastructure.couchbase;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
public class CouchbaseConfigProperty {

    @Value("${couchbase.bootstrapHost}")
    private String bootstrapHost;

    @Value("${couchbase.personBucketName}")
    private String personBucketName;

    @Value("${couchbase.username}")
    private String username;

    @Value("${couchbase.password}")
    private String password;

    @Value("${couchbase.kvTimeoutMs}")
    private Integer kvTimeoutMs;

    @Value("${couchbase.queryTimeoutMs}")
    private Integer queryTimeoutMs;

    @Value("${couchbase.waitUntilReadyMs}")
    private Integer waitUntilReadyMs;

    @Value("${couchbase.connectTimeoutMs}")
    private Integer connectTimeoutMs;

    private Integer testCustomKvPort;
    private Integer testCustomManagerPort;
}
