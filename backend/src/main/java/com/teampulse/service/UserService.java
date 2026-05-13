package com.teampulse.service;

import com.teampulse.entity.User;
import com.teampulse.graphql.input.ProfileInput;
import com.teampulse.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public User updateProfile(User user, ProfileInput input) {
        if (input.getName() != null) {
            user.setName(input.getName());
        }
        if (input.getAvatarUrl() != null) {
            user.setAvatarUrl(input.getAvatarUrl());
        }
        return userRepository.save(user);
    }
}
