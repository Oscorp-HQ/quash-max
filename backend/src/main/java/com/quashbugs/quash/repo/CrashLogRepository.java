package com.quashbugs.quash.repo;

import com.quashbugs.quash.model.CrashLog;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CrashLogRepository extends MongoRepository<CrashLog, String> {
    CrashLog findByLogUrl(String logUrl);
}
