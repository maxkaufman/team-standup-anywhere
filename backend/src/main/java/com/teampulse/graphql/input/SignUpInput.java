package com.teampulse.graphql.input;

import lombok.Data;

@Data
public class SignUpInput {
    private String email;
    private String password;
    private String name;
}
