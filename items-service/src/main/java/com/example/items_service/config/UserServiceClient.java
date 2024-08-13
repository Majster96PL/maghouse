package com.example.items_service.config;

import com.example.user_service.auth.registration.user.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "user-service", url = "${user.service.url}")
public interface UserServiceClient {

    @GetMapping("/users/{id}")
    User getUserById(@RequestHeader("Authorization") String token, @PathVariable("id") Long id);
}
