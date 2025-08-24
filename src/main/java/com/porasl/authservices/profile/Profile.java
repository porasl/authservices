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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
    @Column(
            nullable = false,
            updatable = false
    )
    private LocalDateTime createDate;

    @LastModifiedDate
    @Column(insertable = false)
    private LocalDateTime lastModified;


    @CreatedBy
    @Column(
            nullable = false,
            updatable = false
    )
    private Integer createdBy;

    @LastModifiedBy
    @Column(insertable = false)
    private Integer lastModifiedBy;

    // Manual builder method
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

        public ProfileBuilder id(Integer id) {
            this.id = id;
            return this;
        }

        public ProfileBuilder accountId(Integer accountId) {
            this.accountId = accountId;
            return this;
        }

        public ProfileBuilder type(String type) {
            this.type = type;
            return this;
        }

        public ProfileBuilder contactListId(Integer contactListId) {
            this.contactListId = contactListId;
            return this;
        }

        public ProfileBuilder lang(String lang) {
            this.lang = lang;
            return this;
        }

        public ProfileBuilder country(String country) {
            this.country = country;
            return this;
        }

        public ProfileBuilder county(String county) {
            this.county = county;
            return this;
        }

        public ProfileBuilder city(String city) {
            this.city = city;
            return this;
        }

        public ProfileBuilder agegroup(Integer agegroup) {
            this.agegroup = agegroup;
            return this;
        }

        public ProfileBuilder interestListId(Integer interestListId) {
            this.interestListId = interestListId;
            return this;
        }

        public Profile build() {
            Profile profile = new Profile();
            profile.id = this.id;
            profile.accountId = this.accountId;
            profile.type = this.type;
            profile.contactListId = this.contactListId;
            profile.lang = this.lang;
            profile.country = this.country;
            profile.county = this.county;
            profile.city = this.city;
            profile.agegroup = this.agegroup;
            profile.interestListId = this.interestListId;
            return profile;
        }
    }
}