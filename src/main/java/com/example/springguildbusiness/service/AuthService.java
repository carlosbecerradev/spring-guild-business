package com.example.springguildbusiness.service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.springguildbusiness.dto.AuthenticationResponse;
import com.example.springguildbusiness.dto.LoginRequest;
import com.example.springguildbusiness.dto.RegisterRequest;
import com.example.springguildbusiness.exceptions.SpringException;
import com.example.springguildbusiness.model.NotificationEmail;
import com.example.springguildbusiness.model.User;
import com.example.springguildbusiness.model.VerificationToken;
import com.example.springguildbusiness.repository.UserRepository;
import com.example.springguildbusiness.repository.VerificationTokenRepository;
import com.example.springguildbusiness.security.JwtProvider;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class AuthService {

	private final PasswordEncoder passwordEncoder;
	private final UserRepository userRepository;
	private final VerificationTokenRepository verificationTokenRepository;
	private final MailService mailService;
	private final AuthenticationManager authenticationManager;
	private final JwtProvider jwtProvicer;
	
	@Transactional
	public void signup(RegisterRequest registerRequest) {
		User user = new User();
		user.setUsername(registerRequest.getUsername());
		user.setEmail(registerRequest.getEmail());
		user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
		user.setCreated(Instant.now());
		user.setEnabled(false);
		
		userRepository.save(user);
		
		String token = generateVerificationToken(user);
		mailService.sendMail(new NotificationEmail(
					"Please Activate your Account",
					user.getEmail(),
					"Thank you for signing up to Spring Guild Business, " +
					"please click on the below url to activate your account: " +
					"http://localhost:8080/api/auth/accountVerification/" + token
				));
	}

	private String generateVerificationToken(User user) {
		String token = UUID.randomUUID().toString();
		VerificationToken verificationToken = new VerificationToken();
		verificationToken.setToken(token);
		verificationToken.setUser(user);
		
		verificationTokenRepository.save(verificationToken);
		return token;
	}

	public void verifyAccount(String token) {
		Optional<VerificationToken> verificationToken = verificationTokenRepository.findByToken(token);
		verificationToken.orElseThrow( () -> new SpringException("Invalid Token") );
		fetchUserAndEnabled(verificationToken.get());
	}

	@Transactional
	private void fetchUserAndEnabled(VerificationToken verificationToken) {
		String username = verificationToken.getUser().getUsername();
		User user = userRepository.findByUsername(username)
			.orElseThrow( () -> new SpringException("User not found with name - " + username));
		user.setEnabled(true);
		userRepository.save(user);
	}

	public AuthenticationResponse login(LoginRequest loginRequest) {
		Authentication authenticate = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
			);
		SecurityContextHolder.getContext().setAuthentication(authenticate);
		String token = jwtProvicer.generateToken(authenticate);
		return new AuthenticationResponse(token, loginRequest.getUsername());
	}
}
