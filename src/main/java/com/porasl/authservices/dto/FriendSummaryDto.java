package com.porasl.authservices.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder  
public class FriendSummaryDto {
    private Long id;
    private String email;
    private String firstname;
    private String lastname;
    private String profileImageUrl;
    private Long connectionId;
    private Long since;
}
