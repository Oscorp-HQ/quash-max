package com.quashbugs.quash.repo;

import com.quashbugs.quash.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    User findByUsername(String username);

    Optional<User> findByFullName(String fullName);

    Optional<User> findByWorkEmail(String workEmail);

    Optional<User> findByVerificationToken(String verificationToken);

    List<User> findByWorkEmailIn(List<String> emails);

    List<User> findByWorkEmailEndingWith(String domain);
}