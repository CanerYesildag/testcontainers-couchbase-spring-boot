package com.caner.testcontainers.infrastructure.repository;

import com.caner.testcontainers.domain.Person;
import com.caner.testcontainers.domain.PersonRepository;
import com.couchbase.client.core.error.DocumentNotFoundException;
import com.couchbase.client.java.ReactiveCollection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Slf4j
@Repository
@RequiredArgsConstructor
public class PersonRepositoryImpl implements PersonRepository {

    private final ReactiveCollection personReactiveCollection;

    @Override
    public Person getById(String personId) {
        return personReactiveCollection.get(personId)
                .timeout(Duration.ofMillis(300))
                .map(doc -> doc.contentAs(Person.class))
                .onErrorResume(ex -> {
                    if (ex instanceof DocumentNotFoundException) {
                        log.warn("Document not found by documentId: {}, Exception: ", personId, ex);
                        return Mono.empty();
                    }
                    log.warn("Error occurred when getting document with documentId: {}, Exception: ", personId, ex);
                    return personReactiveCollection.getAnyReplica(personId)
                            .map(doc -> doc.contentAs(Person.class))
                            .onErrorResume(secondEx -> {
                                log.error("Error occurred when getting document from replica with documentId: {}, Exception: ", personId, secondEx);
                                return Mono.empty();
                            });
                }).block();
    }
}
