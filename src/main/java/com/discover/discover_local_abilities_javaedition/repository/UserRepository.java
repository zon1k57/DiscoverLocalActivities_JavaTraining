package com.discover.discover_local_abilities_javaedition.repository;

import com.discover.discover_local_abilities_javaedition.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
