package org.server.rsaga.user.infra.api;

import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.RequiredArgsConstructor;
import org.server.rsaga.common.dto.FullHttpResponseBuilder;
import org.server.rsaga.user.dto.request.ModifyUserBalanceRequest;
import org.server.rsaga.user.dto.request.ModifyUserRequest;
import org.server.rsaga.user.dto.request.RegisterUserRequest;
import org.server.rsaga.user.app.UserApiService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserApiService userApiService;

    @GetMapping("/{user_id}")
    public FullHttpResponse findUser(@PathVariable("user_id") Long userId) {
        return FullHttpResponseBuilder.builder()
                .body(
                        userApiService.findUser(userId)
                )
                .statusCode(HttpResponseStatus.OK)
                .build();
    }

    @PostMapping
    public FullHttpResponse registerUser(@RequestBody RegisterUserRequest request) {
        return FullHttpResponseBuilder.builder()
                .body(
                        userApiService.registerUser(request)
                )
                .statusCode(HttpResponseStatus.OK)
                .build();
    }

    @PutMapping("/{user_id}")
    public FullHttpResponse modifyUser(@PathVariable("user_id") Long userId, @RequestBody ModifyUserRequest request) {
        return FullHttpResponseBuilder.builder()
                .body(
                        userApiService.modifyUser(userId, request)
                )
                .statusCode(HttpResponseStatus.OK)
                .build();
    }

    @PutMapping("/{user_id}/deposit")
    public FullHttpResponse deposit(@PathVariable("user_id") Long userId, @RequestBody ModifyUserBalanceRequest request) {
        return FullHttpResponseBuilder.builder()
                .body(
                        userApiService.deposit(userId, request)
                )
                .statusCode(HttpResponseStatus.OK)
                .build();
    }

    @PutMapping("/{user_id}/withdraw")
    public FullHttpResponse withdraw(@PathVariable("user_id") Long userId, @RequestBody ModifyUserBalanceRequest request) {
        return FullHttpResponseBuilder.builder()
                .body(
                        userApiService.withdraw(userId, request)
                )
                .statusCode(HttpResponseStatus.OK)
                .build();
    }


    @DeleteMapping("/{user_id}")
    public FullHttpResponse deleteUser(@PathVariable("user_id") Long userId) {
        userApiService.deleteUse(userId);
        return FullHttpResponseBuilder.builder()
                .body("Deleted.")
                .statusCode(HttpResponseStatus.OK)
                .build();
    }
}
