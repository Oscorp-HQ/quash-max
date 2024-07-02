package com.quashbugs.quash.repo;

import com.quashbugs.quash.model.Contact;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ContactRepository extends MongoRepository<Contact, String> {

}
