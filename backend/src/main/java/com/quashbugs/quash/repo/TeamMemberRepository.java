package com.quashbugs.quash.repo;

import com.quashbugs.quash.model.Organisation;
import com.quashbugs.quash.model.TeamMember;
import com.quashbugs.quash.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamMemberRepository extends MongoRepository<TeamMember, String> {
    TeamMember findByOrganisationAndUser(Organisation organisation, User user);

    TeamMember findByUser(User user);

    Optional<TeamMember> findByUserAndOrganisation(User user, String organisationKey);

    List<TeamMember> findByOrganisation(Organisation organisation);
}
