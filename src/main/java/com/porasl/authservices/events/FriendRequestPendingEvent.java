package com.porasl.authservices.events;

import java.time.Instant;

public record FriendRequestPendingEvent(
        Long requesterId,
        String requesterName,
        Long targetUserId,
        String targetEmail,
        Instant createdAt
) {}

