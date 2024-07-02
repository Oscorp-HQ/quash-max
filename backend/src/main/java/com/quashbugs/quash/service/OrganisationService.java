package com.quashbugs.quash.service;

import com.quashbugs.quash.dto.request.OrganisationSignUpRequestDTO;
import com.quashbugs.quash.model.Organisation;
import com.quashbugs.quash.model.User;
import com.quashbugs.quash.repo.OrganisationRepository;
import com.quashbugs.quash.util.OrganisationKeyGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class OrganisationService {

    private final OrganisationRepository organisationRepository;

    @Autowired
    public OrganisationService(OrganisationRepository organisationRepository) {
        this.organisationRepository = organisationRepository;
    }

    public Organisation createOrganisation(OrganisationSignUpRequestDTO request, User user) {
        if (organisationRepository.findByCreatedBy(user) != null) {
            throw new IllegalArgumentException("Organisation already exists for this user");
        }

        Organisation organisation = buildOrganisation(request, user);
        return organisationRepository.save(organisation);
    }

    private Organisation buildOrganisation(OrganisationSignUpRequestDTO request, User user) {
        var organisation = Organisation.builder()
                .name(request.getOrganisationName())
                .createdBy(user)
                .createdAt(new Date())
                .build();
        organisation.setOrgAbbreviation(generateUniqueAbbreviation(organisation));
        organisation.setOrgUniqueKey(OrganisationKeyGenerator.generateApiKey(organisation));
        return organisation;
    }

    public String generateUniqueAbbreviation(Organisation organisation) {
        String abbreviation = OrganisationKeyGenerator.generateAbbreviation(organisation);
        String originalAbbreviation = abbreviation;
        int counter = 1;

        while (organisationRepository.findOptionalByOrgAbbreviation(abbreviation).isPresent()) {
            abbreviation = originalAbbreviation + counter;
            counter++;
        }
        return abbreviation;
    }

    public List<Organisation> getAllOrganisations() {
        return organisationRepository.findAll();
    }

    public Organisation getOrganisation(User user) {
        return organisationRepository.findByCreatedBy(user);
    }

    public Optional<Organisation> getOrganisationByName(String name) {
        return organisationRepository.findOptionalByName(name);
    }

    public void deleteOrganisation(User user) throws Exception {
        var organisation = organisationRepository.findByCreatedBy(user);
        if (organisation != null) {
            organisationRepository.delete(organisation);
        } else {
            throw new Exception("Organisation not found for this user");
        }
    }
}