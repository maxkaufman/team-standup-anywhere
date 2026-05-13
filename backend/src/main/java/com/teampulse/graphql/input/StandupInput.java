package com.teampulse.graphql.input;

import lombok.Data;

@Data
public class StandupInput {
    private String yesterday;
    private String today;
    private String blockers;
    private int mood;
}
