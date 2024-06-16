package com.inrik.authservices.profile;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private ProfileRepository repository;

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

