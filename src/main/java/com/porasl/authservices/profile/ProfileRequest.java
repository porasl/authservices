package com.porasl.authservices.profile;

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

    public ProfileRequest() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Integer getAccountId() { return accountId; }
    public void setAccountId(Integer accountId) { this.accountId = accountId; }
    public Integer getContactListId() { return contactListId; }
    public void setContactListId(Integer contactListId) { this.contactListId = contactListId; }
    public String getLang() { return lang; }
    public void setLang(String lang) { this.lang = lang; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public String getCounty() { return county; }
    public void setCounty(String county) { this.county = county; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public Integer getAgegroup() { return agegroup; }
    public void setAgegroup(Integer agegroup) { this.agegroup = agegroup; }
    public Integer getInterestListId() { return interestListId; }
    public void setInterestListId(Integer interestListId) { this.interestListId = interestListId; }
}