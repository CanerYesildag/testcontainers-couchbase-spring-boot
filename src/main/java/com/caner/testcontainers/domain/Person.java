package com.caner.testcontainers.domain;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Person {

    private String id;
    private String name;
    private String surname;
    private String gender;
    private String job;
}
