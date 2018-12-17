package com.teodorus.helpdesk.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.teodorus.helpdesk.domain.User;
import com.teodorus.helpdesk.security.CurrentUser;
import com.teodorus.helpdesk.security.jwt.JwtAuthenticationRequest;
import com.teodorus.helpdesk.security.jwt.JwtTokenUtil;
import com.teodorus.helpdesk.service.UserService;

@RestController
@CrossOrigin(origins = "*")
public class AuthenticationRestController {

	@Autowired
	private AuthenticationManager manager;
	
	@Autowired
	private JwtTokenUtil jwtTokentUtil;
	
	@Autowired
	private UserDetailsService userDetailService;
	
	@Autowired
	private UserService service;
	
	@PostMapping(value = "/api/auth")
	public ResponseEntity<?> createAuthenticationToken(@RequestBody JwtAuthenticationRequest request) throws Exception {
		final Authentication auth = manager.authenticate(
				new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
		SecurityContextHolder.getContext().setAuthentication(auth);
		final UserDetails userDetails = userDetailService.loadUserByUsername(request.getEmail());
		final String token = jwtTokentUtil.generateToken(userDetails);
		final User user = service.findByEmail(request.getEmail());
		user.setPassword(null);
		return ResponseEntity.ok(new CurrentUser(token, user));
	}
	
	@PostMapping(value = "/api/refresh")
	public ResponseEntity<?> refreshAndGetAuthenticationToken(HttpServletRequest request) throws Exception {
		String token = request.getHeader("Authorization");
		String username = jwtTokentUtil.getUserNameFromToken(token);
		final User user = service.findByEmail(username);
		
		if (jwtTokentUtil.canTokenBeRefreshed(token)) {
			String refreshedToken = jwtTokentUtil.refreshToken(token);
			return ResponseEntity.ok(new CurrentUser(refreshedToken, user));
		} else {
			return ResponseEntity.badRequest().body(null);
		}
		
	}
	
}
