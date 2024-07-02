package com.quashbugs.quash.repo;

import com.quashbugs.quash.model.Organisation;
import com.quashbugs.quash.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrganisationRepository extends MongoRepository<Organisation, Long> {
    Organisation findByCreatedBy(User user);

    Optional<Organisation> findOptionalByCreatedBy(User user);

    Optional<Organisation> findOptionalByOrgUniqueKey(String orgUniqueKey);

    Optional<Organisation> findOptionalByName(String name);

    Optional<Organisation> findOptionalByOrgAbbreviation(String orgAbbreviation);

    Optional<Organisation> findOptionalById(Long id);
}
