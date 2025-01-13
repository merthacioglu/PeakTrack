package org.mhacioglu.peaktrackserver.repository;

import org.mhacioglu.peaktrackserver.model.RegisteredUser;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserRepository extends CrudRepository<RegisteredUser, Long> {
    Optional<RegisteredUser> findByUsername(String username);
}
