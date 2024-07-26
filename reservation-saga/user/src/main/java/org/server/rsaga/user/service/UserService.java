package org.server.rsaga.user.service;

import lombok.RequiredArgsConstructor;
import org.server.rsaga.common.exception.CustomException;
import org.server.rsaga.common.exception.ErrorCode;
import org.server.rsaga.user.dto.ModifyUserRequest;
import org.server.rsaga.user.dto.RegisterUserRequest;
import org.server.rsaga.user.entity.User;
import org.server.rsaga.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public User findUser(Long userId) {
        return findUserOrElseThrow(userId);
    }

    private User findUserOrElseThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(
                        () -> new CustomException(ErrorCode.USER_NOT_FOUND)
                );
    }

    @Transactional
    public User registerUser(RegisterUserRequest request) {
        checkUniqueUserName(request.name());

        User user = User.builder()
                .name(request.name())
                // TODO : Test 를 위해
                .balance(100_000_000L)
                .build();
        return userRepository.save(user);
    }

    private void checkUniqueUserName(String name) {
        userRepository.findByName(name)
                .ifPresent(user -> {
                    throw new CustomException(ErrorCode.USER_NAME_ALREADY_EXIST);
                });
    }

    @Transactional
    public User modifyUser(Long userId, ModifyUserRequest request) {
        checkUniqueUserName(request.name());

        User user = findUserOrElseThrow(userId);

        user
                .setName(
                        Objects.requireNonNull(request.name(), user.getName())
                );

        return user;
    }

    @Transactional
    public void deleteUse(Long userId) {
        User user = findUserOrElseThrow(userId);
        userRepository.delete(user);
    }
}
