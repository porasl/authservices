package com.porasl.authservices.dto;

import java.util.List;

public record UserDTO(
        Long id,
        String email,
        String firstName,
        String lastName,
        String profileImageUrl,
        List<Long> sentConnectionIds,
        List<Long> receivedConnectionIds
) {}
