package com.example.maghouse.auth.registration.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findUserByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.role= :role")
    Optional<User> findByRoleName(@Param("role") String role);
}
