package com.quashbugs.quash.repo;

import com.quashbugs.quash.model.BugMedia;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface BugMediaRepository extends MongoRepository<BugMedia, String> {
    BugMedia findByMediaUrl(String mediaUrl);
}
