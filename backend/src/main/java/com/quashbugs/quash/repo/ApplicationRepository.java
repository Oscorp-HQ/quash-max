package com.quashbugs.quash.repo;

import com.quashbugs.quash.model.Organisation;
import com.quashbugs.quash.model.QuashClientApplication;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ApplicationRepository extends MongoRepository<QuashClientApplication, String> {
    Optional<QuashClientApplication> findByPackageName(String packageName);

    Optional<QuashClientApplication> findByPackageNameAndOrganisation(String packageName, Organisation organisation);

    List<QuashClientApplication> findAllByOrganisation(Organisation organisation);

    Optional<QuashClientApplication> findById(String appId);
}
