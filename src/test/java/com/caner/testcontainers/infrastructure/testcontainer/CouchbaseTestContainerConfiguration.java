package com.caner.testcontainers.infrastructure.testcontainer;

import com.caner.testcontainers.infrastructure.couchbase.CouchbaseConfigProperty;
import com.couchbase.client.core.endpoint.CircuitBreakerConfig;
import com.couchbase.client.core.env.IoConfig;
import com.couchbase.client.core.env.SeedNode;
import com.couchbase.client.core.env.TimeoutConfig;
import com.couchbase.client.core.retry.BestEffortRetryStrategy;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.ClusterOptions;
import com.couchbase.client.java.ReactiveCluster;
import com.couchbase.client.java.codec.JacksonJsonSerializer;
import com.couchbase.client.java.env.ClusterEnvironment;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.couchbase.BucketDefinition;
import org.testcontainers.couchbase.CouchbaseContainer;
import org.testcontainers.couchbase.CouchbaseService;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = CouchbaseTestConfiguration.class)
public abstract class CouchbaseTestContainerConfiguration {

    public static final CouchbaseContainer COUCHBASE_CONTAINER;
    private static final String PERSON_BUCKET_NAME = "Person";
    private static final String COUCHBASE_IMAGE_PATH = "couchbase/server:7.0.0";

    static {
        COUCHBASE_CONTAINER = new CouchbaseContainer(DockerImageName.parse(COUCHBASE_IMAGE_PATH)
                .asCompatibleSubstituteFor("couchbase/server"))
                .withBucket(new BucketDefinition(PERSON_BUCKET_NAME)
                        .withQuota(101)
                        .withPrimaryIndex(false))
                .withSharedMemorySize(128000000L)
                .withEnabledServices(CouchbaseService.INDEX, CouchbaseService.KV, CouchbaseService.QUERY)
                .withExposedPorts(8091, 8093, 11210, 11211)
                .waitingFor(Wait.defaultWaitStrategy());

        COUCHBASE_CONTAINER.withStartupTimeout(Duration.ofSeconds(100));
        COUCHBASE_CONTAINER.start();
    }
}

@TestConfiguration
class CouchbaseTestConfiguration {

    @Bean
    @Primary
    public CouchbaseConfigProperty createMockCouchbaseConfigPropertyBean() {
        CouchbaseConfigProperty configProperty = Mockito.mock(CouchbaseConfigProperty.class);

        String bootstrapHost = CouchbaseTestContainerConfiguration.COUCHBASE_CONTAINER.getContainerIpAddress();
        Integer customKvPort = CouchbaseTestContainerConfiguration.COUCHBASE_CONTAINER.getMappedPort(11210);
        Integer customManagerPort = CouchbaseTestContainerConfiguration.COUCHBASE_CONTAINER.getMappedPort(8091);

        Mockito.doReturn(bootstrapHost).when(configProperty).getBootstrapHost();
        Mockito.doReturn(10000).when(configProperty).getConnectTimeoutMs();
        Mockito.doReturn(300).when(configProperty).getKvTimeoutMs();
        Mockito.doReturn(5000).when(configProperty).getQueryTimeoutMs();
        Mockito.doReturn(10000).when(configProperty).getWaitUntilReadyMs();
        Mockito.doReturn("Person").when(configProperty).getPersonBucketName();
        Mockito.doReturn("Administrator").when(configProperty).getUsername();
        Mockito.doReturn("password").when(configProperty).getPassword();

        Mockito.doReturn(customKvPort).when(configProperty).getTestCustomKvPort();
        Mockito.doReturn(customManagerPort).when(configProperty).getTestCustomManagerPort();

        return configProperty;
    }

    @Bean
    @Primary
    public ObjectMapper createMockObjectMapperBean() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        return mapper;
    }

    @Bean
    @Primary
    public ReactiveCluster createMockClusterBean() {
        ClusterEnvironment clusterEnvironment = ClusterEnvironment.builder()
                .jsonSerializer(JacksonJsonSerializer.create(createMockObjectMapperBean()))
                .ioConfig(IoConfig.kvCircuitBreakerConfig(CircuitBreakerConfig.builder()
                        .enabled(false)))
                .timeoutConfig(TimeoutConfig
                        .connectTimeout(Duration.ofMillis(createMockCouchbaseConfigPropertyBean().getConnectTimeoutMs()))
                        .kvTimeout(Duration.ofMillis(createMockCouchbaseConfigPropertyBean().getKvTimeoutMs()))
                        .queryTimeout(Duration.ofMillis(createMockCouchbaseConfigPropertyBean().getQueryTimeoutMs())))
                .retryStrategy(BestEffortRetryStrategy.INSTANCE)
                .build();

        final Set<SeedNode> seedNodes = new HashSet<>(Collections.singletonList(
                SeedNode.create(createMockCouchbaseConfigPropertyBean().getBootstrapHost(),
                        Optional.of(createMockCouchbaseConfigPropertyBean().getTestCustomKvPort()),
                        Optional.of(createMockCouchbaseConfigPropertyBean().getTestCustomManagerPort()))));

        return Cluster.connect(
                seedNodes,
                ClusterOptions.clusterOptions(
                                createMockCouchbaseConfigPropertyBean().getUsername(),
                                createMockCouchbaseConfigPropertyBean().getPassword())
                        .environment(clusterEnvironment)).reactive();
    }
}
