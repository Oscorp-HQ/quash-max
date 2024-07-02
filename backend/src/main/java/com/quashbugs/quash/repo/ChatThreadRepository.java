package com.quashbugs.quash.repo;

import com.quashbugs.quash.model.ChatThread;
import com.quashbugs.quash.model.Report;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ChatThreadRepository extends MongoRepository<ChatThread, String> {
    List<ChatThread> findByReport(Report report);
}
