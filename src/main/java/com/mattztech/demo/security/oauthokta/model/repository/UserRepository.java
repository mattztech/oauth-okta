package com.mattztech.demo.security.oauthokta.model.repository;

import com.mattztech.demo.security.oauthokta.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {}
