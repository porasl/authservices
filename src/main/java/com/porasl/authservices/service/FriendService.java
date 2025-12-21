package com.porasl.authservices.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.porasl.common.dto.FriendSummaryDto;
import com.porasl.authservices.user.User;
import com.porasl.authservices.user.UserRepository;

@Service
public class FriendService {
    private final UserRepository userRepo;

    public FriendService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

	public List<FriendSummaryDto> listFriends(User requester) {
		// TODO Auto-generated method stub
		return null;
	}

    // ... methods here
}