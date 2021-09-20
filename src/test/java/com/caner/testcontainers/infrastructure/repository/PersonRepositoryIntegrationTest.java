package com.caner.testcontainers.infrastructure.repository;

import com.caner.testcontainers.domain.Person;
import com.caner.testcontainers.domain.PersonRepository;
import com.caner.testcontainers.infrastructure.testcontainer.CouchbaseTestContainerConfiguration;
import com.caner.testcontainers.infrastructure.testcontainer.helper.PersonRepositoryTestHelper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PersonRepositoryIntegrationTest extends CouchbaseTestContainerConfiguration {

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private PersonRepositoryTestHelper personRepositoryTestHelper;

    @Test
    void getById_WhenCalledWithPersonId_ShouldReturnPersonDocument() {
        //given
        Person mockPerson = Person.builder()
                .id(UUID.randomUUID().toString())
                .name("John")
                .surname("Smith")
                .job("Actor")
                .gender("Male").build();

        personRepositoryTestHelper.insert(mockPerson);

        //when
        Person retrievedPerson = personRepository.getById(mockPerson.getId());

        //then
        assertThat(retrievedPerson).usingRecursiveComparison().isEqualTo(mockPerson);
    }
}