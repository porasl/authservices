package com.porasl.authservices.profile;


import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "profile")
@EntityListeners(AuditingEntityListener.class)
public class Profile {

    @Id
    @GeneratedValue
    private Integer id;
    private Integer accountId;
    private String type;
    private Integer contactListId;
    private String lang;
    private String country;
    private String county;
    private String city;
    private Integer agegroup;
    private Integer interestListId;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createDate;

    @LastModifiedDate
    @Column(insertable = false)
    private LocalDateTime lastModified;

    @CreatedBy
    @Column(nullable = false, updatable = false)
    private Integer createdBy;

    @LastModifiedBy
    @Column(insertable = false)
    private Integer lastModifiedBy;

    public Profile() {}

    public Profile(Integer id, Integer accountId, String type, Integer contactListId, String lang, String country, String county, String city, Integer agegroup, Integer interestListId) {
        this.id = id;
        this.accountId = accountId;
        this.type = type;
        this.contactListId = contactListId;
        this.lang = lang;
        this.country = country;
        this.county = county;
        this.city = city;
        this.agegroup = agegroup;
        this.interestListId = interestListId;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getAccountId() { return accountId; }
    public void setAccountId(Integer accountId) { this.accountId = accountId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
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

    public static ProfileBuilder builder() {
        return new ProfileBuilder();
    }

    public static class ProfileBuilder {
        private Integer id;
        private Integer accountId;
        private String type;
        private Integer contactListId;
        private String lang;
        private String country;
        private String county;
        private String city;
        private Integer agegroup;
        private Integer interestListId;

        public ProfileBuilder id(Integer id) { this.id = id; return this; }
        public ProfileBuilder accountId(Integer accountId) { this.accountId = accountId; return this; }
        public ProfileBuilder type(String type) { this.type = type; return this; }
        public ProfileBuilder contactListId(Integer contactListId) { this.contactListId = contactListId; return this; }
        public ProfileBuilder lang(String lang) { this.lang = lang; return this; }
        public ProfileBuilder country(String country) { this.country = country; return this; }
        public ProfileBuilder county(String county) { this.county = county; return this; }
        public ProfileBuilder city(String city) { this.city = city; return this; }
        public ProfileBuilder agegroup(Integer agegroup) { this.agegroup = agegroup; return this; }
        public ProfileBuilder interestListId(Integer interestListId) { this.interestListId = interestListId; return this; }

        public Profile build() {
            return new Profile(id, accountId, type, contactListId, lang, country, county, city, agegroup, interestListId);
        }
    }
}