package com.quashbugs.quash.repo;

import com.quashbugs.quash.model.Integration;
import com.quashbugs.quash.model.Organisation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface IntegrationRepository extends MongoRepository<Integration, String> {
    Optional<Integration> findByOrganisation(Organisation byCreatedBy);

    Optional<Integration> findByOrganisationAndIntegrationType(Organisation organisation, String type);

    List<Integration> findAllByOrganisation(Organisation organisation);

    Optional<Integration> findIntegrationByIntegrationType(String type);

    @Query("{ 'settings.teamId' : ?0 }")
    Optional<Integration> findBySettingsTeamId(String teamId);
}