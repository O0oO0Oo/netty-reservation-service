package org.server.reservation.user.controller;

import org.server.reservation.core.common.dto.FullHttpResponseBuilder;
import org.server.reservation.user.dto.ModifyUserRequest;
import org.server.reservation.user.dto.RegisterUserRequest;
import org.server.reservation.user.service.UserService;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/{user_id}")
    public FullHttpResponse findUser(@PathVariable("user_id") Long userId) {
        return FullHttpResponseBuilder.builder()
                .body(
                        userService.findUser(userId)
                )
                .statusCode(HttpResponseStatus.OK)
                .build();
    }

    @PostMapping
    public FullHttpResponse registerUser(@RequestBody RegisterUserRequest request) {
        return FullHttpResponseBuilder.builder()
                .body(
                        userService.registerUser(request)
                )
                .statusCode(HttpResponseStatus.OK)
                .build();
    }

    @PutMapping("/{user_id}")
    public FullHttpResponse modifyUser(@PathVariable("user_id") Long userId, @RequestBody ModifyUserRequest request) {
        return FullHttpResponseBuilder.builder()
                .body(
                        userService.modifyUser(userId, request)
                )
                .statusCode(HttpResponseStatus.OK)
                .build();
    }

    @DeleteMapping("/{user_id}")
    public FullHttpResponse deleteUser(@PathVariable("user_id") Long userId) {
        userService.deleteUse(userId);
        return FullHttpResponseBuilder.builder()
                .body("Deleted.")
                .statusCode(HttpResponseStatus.OK)
                .build();
    }
}
