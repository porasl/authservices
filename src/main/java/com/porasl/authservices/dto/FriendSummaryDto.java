package com.porasl.authservices.dto;

import lombok.Builder;

@Builder
public record FriendSummaryDto(
		Long connectionId,
        String email,
        String firstname,
        String lastname,
        String profileImageUrl,
        Long since,
        String notes,
        long requesterId,
        long targetId
) {}