package com.porasl.authservices.profile;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProfileService {

    private final ProfileRepository repository;

    @Autowired
    public ProfileService(ProfileRepository repository) {
        this.repository = repository;
    }

    public void save(ProfileRequest request) {
        var profile = Profile.builder()
                .id(request.getId())
                .type(request.getType())
                .accountId(request.getAccountId())
                .contactListId(request.getContactListId())
                .interestListId(request.getInterestListId())
                .lang(request.getLang())
                .country(request.getCountry())
                .city(request.getCity())
                .agegroup(request.getAgegroup())
                .build();
        repository.save(profile);
    }

    public List<Profile> findAll() {
        return repository.findAll();
    }
}