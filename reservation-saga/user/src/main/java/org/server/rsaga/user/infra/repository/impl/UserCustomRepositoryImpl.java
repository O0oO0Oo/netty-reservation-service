package org.server.rsaga.user.infra.repository.impl;

import lombok.RequiredArgsConstructor;
import org.server.rsaga.common.exception.CustomException;
import org.server.rsaga.common.exception.ErrorCode;
import org.server.rsaga.user.domain.User;
import org.server.rsaga.user.infra.repository.UserJpaRepository;
import org.server.rsaga.user.infra.repository.UserCustomRepository;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserCustomRepositoryImpl implements UserCustomRepository {
    private final UserJpaRepository userJpaRepository;

    @Override
    public User findByIdOrElseThrow(Long id) {
        return userJpaRepository.findById(id)
                .orElseThrow(
                        () -> new CustomException(ErrorCode.USER_NOT_FOUND)
                );
    }

    @Override
    public void validateUniqueUserName(String name) {
        userJpaRepository.findByName(name)
                .ifPresent(user -> {
                    throw new CustomException(ErrorCode.USER_NAME_ALREADY_EXIST);
                });
    }
}
