package com.quashbugs.quash.repo;

import com.quashbugs.quash.model.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ReportRepository extends MongoRepository<Report, String> {
    Page<Report> findByAppId(String appId, Pageable pageable);

    List<Report> findByAppId(String appId);

    void deleteByIdIn(List<String> ids);
}
