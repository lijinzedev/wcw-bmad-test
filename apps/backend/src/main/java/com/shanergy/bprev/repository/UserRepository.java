package com.shanergy.bprev.repository;

import com.shanergy.bprev.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    @EntityGraph(attributePaths = {"roles"})
    Optional<User> findByUsername(String username);
}

