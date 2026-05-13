package com.teampulse.graphql.resolver;

import com.teampulse.entity.Standup;
import com.teampulse.entity.User;
import com.teampulse.service.StandupService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class UserResolver {

    private final StandupService standupService;

    @SchemaMapping(typeName = "User", field = "standups")
    public List<Standup> standups(User user) {
        return standupService.getMyStandups(user.getId(), 20, 0);
    }

    @SchemaMapping(typeName = "User", field = "createdAt")
    public String createdAt(User user) {
        return user.getCreatedAt() != null ? user.getCreatedAt().toString() : null;
    }
}
