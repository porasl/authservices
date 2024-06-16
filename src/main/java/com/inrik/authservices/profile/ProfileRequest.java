package com.inrik.authservices.profile;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ProfileRequest {

    private Integer id;
    private String type;
    private Integer accountId;
    private Integer contactListId;
    private String lang;
    private String country;
    private String county;
    private String city;
    private Integer agegroup;
    private Integer interestListId;
}
