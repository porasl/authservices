package com.porasl.authservices.profile;

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

    // Manual getters to ensure compilation
    public Integer getId() { return id; }
    public String getType() { return type; }
    public Integer getAccountId() { return accountId; }
    public Integer getContactListId() { return contactListId; }
    public Integer getInterestListId() { return interestListId; }
    public String getLang() { return lang; }
    public String getCountry() { return country; }
    public String getCity() { return city; }
    public Integer getAgegroup() { return agegroup; }
}
