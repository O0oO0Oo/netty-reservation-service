package org.server.rsaga.user.infra.repository;

import org.server.rsaga.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<User, Long> {
    Optional<User> findByName(String name);
}
