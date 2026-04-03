package com.example.documentmanagementbackend.service;


import com.example.documentmanagementbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserRepositoryDetailsService implements UserDetailsService {
	@Autowired
	private UserRepository repository;

	@Override
	@Transactional
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		// TODO Auto-generated method stub
		return repository.findUserByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User with username: '" + email + "' not found"));
	}

}



