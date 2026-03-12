package com.rev.app.repository;

import com.rev.app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    // Get users by status
    List<User> findByIsActive(int isActive);

    // Count users by status
    long countByIsActive(int isActive);
    
    List<User> findByRole(User.Role role);

}