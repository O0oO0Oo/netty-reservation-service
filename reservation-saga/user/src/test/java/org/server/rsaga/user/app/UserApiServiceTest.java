package org.server.rsaga.user.app;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.server.rsaga.user.domain.User;
import org.server.rsaga.user.dto.request.ModifyUserRequest;
import org.server.rsaga.user.dto.request.RegisterUserRequest;
import org.server.rsaga.user.dto.response.UserDetailsResponse;
import org.server.rsaga.user.infra.repository.UserCustomRepository;
import org.server.rsaga.user.infra.repository.UserJpaRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@DisplayName("UserApiService tests")
@ExtendWith(MockitoExtension.class)
class UserApiServiceTest {
    @Mock
    UserJpaRepository userJpaRepository;

    @Mock
    UserCustomRepository userCustomRepository;

    @InjectMocks
    UserApiService userApiService;

    User user;
    Long userId;

    @BeforeEach
    void setUp() {
        userId = 1L;
        user = new User("testUser");
    }

    @Test
    @DisplayName("registerUser() - valid request - success")
    void should_returnUserDetails_when_registerUser() {
        // given
        RegisterUserRequest request = new RegisterUserRequest("testUser");
        doNothing().when(userCustomRepository).validateUniqueUserName(request.name());
        when(userJpaRepository.save(any(User.class))).thenReturn(user);

        // when
        UserDetailsResponse response = userApiService.registerUser(request);

        // then
        assertNotNull(response);
        assertEquals(request.name(), response.name());
        verify(userCustomRepository).validateUniqueUserName(request.name());
        verify(userJpaRepository).save(any(User.class));
        verify(userJpaRepository, times(1)).save(any(User.class));
        verifyNoMoreInteractions(userJpaRepository);
    }

    @Test
    @DisplayName("modifyUser() - valid request - success")
    void should_returnModifiedUserDetails_when_modifyUser() {
        // given
        ModifyUserRequest request = new ModifyUserRequest("modifiedUser");
        doNothing().when(userCustomRepository).validateUniqueUserName(request.name());
        when(userCustomRepository.findByIdOrElseThrow(userId)).thenReturn(user);

        // when
        UserDetailsResponse response = userApiService.modifyUser(userId, request);

        // then
        assertNotNull(response);
        assertEquals(request.name(), response.name());
        verify(userCustomRepository, times(1)).validateUniqueUserName(request.name());
        verify(userCustomRepository, times(1)).findByIdOrElseThrow(userId);
    }

    @Test
    @DisplayName("deleteUser() - valid request - success")
    void should_deleteUser_when_deleteUser() {
        // given
        when(userCustomRepository.findByIdOrElseThrow(userId)).thenReturn(user);

        // when
        userApiService.deleteUse(userId);

        // then
        verify(userCustomRepository, only()).findByIdOrElseThrow(userId);
        verify(userJpaRepository, only()).delete(user);
    }
}