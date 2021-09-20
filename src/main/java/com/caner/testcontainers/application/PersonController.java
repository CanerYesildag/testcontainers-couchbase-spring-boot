package com.caner.testcontainers.application;

import com.caner.testcontainers.domain.Person;
import com.caner.testcontainers.domain.PersonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PersonController {

    private final PersonRepository personRepository;

    @GetMapping("person/{personId}")
    public ResponseEntity<Person> getById(@PathVariable String personId) {
        Person person = personRepository.getById(personId);
        return ResponseEntity.ok(person);
    }
}
