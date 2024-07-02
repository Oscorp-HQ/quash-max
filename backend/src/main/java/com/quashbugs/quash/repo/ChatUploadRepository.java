package com.quashbugs.quash.repo;

import com.quashbugs.quash.model.ChatThread;
import com.quashbugs.quash.model.ChatUploads;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ChatUploadRepository extends MongoRepository<ChatUploads, String> {
    List<ChatUploads> findByChatThread(ChatThread thread);
}
