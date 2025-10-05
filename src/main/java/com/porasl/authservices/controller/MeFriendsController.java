package com.porasl.authservices.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.porasl.authservices.dto.FriendSummaryDto;
import com.porasl.authservices.service.FriendService;
import com.porasl.authservices.user.User;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth/api/me/friends")
@RequiredArgsConstructor
public class MeFriendsController {

    private final FriendService friendService;

    @GetMapping
    public List<FriendSummaryDto> listFriends(@AuthenticationPrincipal User requester) {
        return friendService.listFriends(requester);
    }
}
