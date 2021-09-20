package com.caner.testcontainers.infrastructure.testcontainer.helper;

import com.caner.testcontainers.domain.Person;
import com.couchbase.client.java.ReactiveCollection;
import com.couchbase.client.java.kv.InsertOptions;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class PersonRepositoryTestHelper {

    private final ReactiveCollection personReactiveCollection;

    public void insert(Person person) {
        personReactiveCollection.insert(
                        person.getId(),
                        person,
                        InsertOptions.insertOptions().timeout(Duration.ofSeconds(100)))
                .block();
    }
}
