package com.quashbugs.quash.repo;

import com.quashbugs.quash.model.GifBitmap;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface GifMediaRepository extends MongoRepository<GifBitmap, String> {
}
