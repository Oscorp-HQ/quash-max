package com.quashbugs.quash.repo;

import com.quashbugs.quash.model.NetworkLog;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface NetworkRepository extends MongoRepository<NetworkLog, String> {
    List<NetworkLog> findByReportId(String reportId);

    Long deleteByReportId(String reportId);
}
