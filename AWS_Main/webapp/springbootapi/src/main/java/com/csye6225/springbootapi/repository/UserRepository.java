package com.csye6225.springbootapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.csye6225.springbootapi.pojo.User;

public interface UserRepository extends JpaRepository<User, String> {
	// List<User> findUserByEmail(String username);
    //Because we use the email rather than id to get user, we need to tell the jpa how to do it
	@Query(value = "SELECT * FROM users WHERE email = ?1", nativeQuery = true)
	User findOneByEmail(String username);
}
