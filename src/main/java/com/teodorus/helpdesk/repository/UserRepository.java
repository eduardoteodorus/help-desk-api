package com.teodorus.helpdesk.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.teodorus.helpdesk.domain.User;

public interface UserRepository extends MongoRepository<User, String>{
	
	public User findByEmail(String email);

}
