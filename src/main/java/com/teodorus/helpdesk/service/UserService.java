package com.teodorus.helpdesk.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.teodorus.helpdesk.domain.User;
import com.teodorus.helpdesk.repository.UserRepository;

@Service
public class UserService {
	
	@Autowired
	private UserRepository userRepository;
	
	public User findByEmail(String email) {
		return this.userRepository.findByEmail(email);
	}
	
	public User createOrUpdate(User user) {
		return this.userRepository.save(user);
	}

	public User findById(String id) {
		Optional<User> optionalUser = this.userRepository.findById(id);
		return optionalUser.get();
	}
	
	public void delete(String id) {
		this.userRepository.deleteById(id);
	}
	
	public Page<User> findAll(int page, int size) {
		Pageable pages = PageRequest.of(page, size);
		return this.userRepository.findAll(pages);
		
	}
	
	
}
