package com.quashbugs.quash.repo;

import com.quashbugs.quash.model.DeviceMetadata;
import com.quashbugs.quash.model.Organisation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeviceMetaRepository extends MongoRepository<DeviceMetadata, String> {
    Optional<DeviceMetadata> findByDeviceAndOsAndOrganisation(String device, String os, Organisation organisation);
}
