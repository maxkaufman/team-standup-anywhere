package com.teampulse.graphql.resolver;

import com.teampulse.entity.Standup;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

@Controller
public class StandupResolver {

    @SchemaMapping(typeName = "Standup", field = "createdAt")
    public String createdAt(Standup standup) {
        return standup.getCreatedAt() != null ? standup.getCreatedAt().toString() : null;
    }
}
