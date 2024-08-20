package org.server.rsaga.user.infra.repository;

import org.server.rsaga.user.domain.User;

public interface UserCustomRepository {
    User findByIdOrElseThrow(Long id);

    void validateUniqueUserName(String name);
}
