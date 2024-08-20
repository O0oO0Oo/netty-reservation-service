package org.server.rsaga.user.app;

import lombok.RequiredArgsConstructor;
import org.server.rsaga.common.domain.Money;
import org.server.rsaga.user.domain.User;
import org.server.rsaga.user.dto.request.ModifyUserBalanceRequest;
import org.server.rsaga.user.dto.request.ModifyUserRequest;
import org.server.rsaga.user.dto.request.RegisterUserRequest;
import org.server.rsaga.user.dto.response.UserDetailsResponse;
import org.server.rsaga.user.infra.repository.UserCustomRepository;
import org.server.rsaga.user.infra.repository.UserJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserApiService {
    private final UserJpaRepository userJpaRepository;
    private final UserCustomRepository userCustomRepository;

    @Transactional(readOnly = true)
    public UserDetailsResponse findUser(Long userId) {
        return UserDetailsResponse.of(
                userCustomRepository.findByIdOrElseThrow(userId)
        );
    }

    @Transactional
    public UserDetailsResponse registerUser(RegisterUserRequest request) {
        userCustomRepository.validateUniqueUserName(request.name());

        User user = new User(
                request.name(),
                new Money(0L)
        );
        return UserDetailsResponse.of(userJpaRepository.save(user));
    }

    @Transactional
    public UserDetailsResponse modifyUser(Long userId, ModifyUserRequest request) {
        userCustomRepository.validateUniqueUserName(request.name());

        User user = userCustomRepository.findByIdOrElseThrow(userId);

        user
                .changeName(
                        request.name()
                );
        return UserDetailsResponse.of(user);
    }

    @Transactional
    public UserDetailsResponse deposit(Long userId, ModifyUserBalanceRequest request) {
        User user = userCustomRepository.findByIdOrElseThrow(userId);

        user
                .addBalance(
                        new Money(
                                request.money()
                        )
                );
        return UserDetailsResponse.of(user);
    }


    @Transactional
    public UserDetailsResponse withdraw(Long userId, ModifyUserBalanceRequest request) {
        User user = userCustomRepository.findByIdOrElseThrow(userId);

        user
                .subtractBalance(
                        new Money(
                                request.money()
                        )
                );
        return UserDetailsResponse.of(user);
    }

    @Transactional
    public void deleteUse(Long userId) {
        User user = userCustomRepository.findByIdOrElseThrow(userId);
        userJpaRepository.delete(user);
    }
}
