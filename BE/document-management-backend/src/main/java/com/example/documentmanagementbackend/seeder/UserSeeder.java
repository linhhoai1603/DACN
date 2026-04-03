package com.example.documentmanagementbackend.seeder;

import com.example.documentmanagementbackend.model.Role;
import com.example.documentmanagementbackend.model.User;
import com.example.documentmanagementbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class UserSeeder implements CommandLineRunner {
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	@Override
	public void run(String... args) {
		List<User> seedUsers = List.of(
				buildUser("Nguyen Van A", "Ha Noi", "vana@example.com", "0912345678", "Password@123", Role.USER),
				buildUser("Tran Thi B", "Da Nang", "thib@example.com", "0987654321", "Btran@456", Role.MANAGER),
				buildUser("Le Van C", "Ho Chi Minh", "vanc@example.com", "0971234567", "Cpass@789", Role.USER),
				buildUser("Pham Thi D", "Can Tho", "thid@example.com", "0351234567", "Dsecure@012", Role.ADMIN),
				buildUser("Hoang Van E", "Hai Phong", "vane@example.com", "0387654321", "Elogin@345", Role.USER)
		);

		List<User> usersToSave = new ArrayList<>();
		for (User user : seedUsers) {
			boolean emailExists = userRepository.findByEmail(user.getEmail()).isPresent();
			boolean phoneExists = userRepository.findByPhone(user.getPhone()).isPresent();
			if (!emailExists && !phoneExists) {
				usersToSave.add(user);
			}
		}

		if (!usersToSave.isEmpty()) {
			userRepository.saveAll(usersToSave);
		}
	}

	private User buildUser(String fullName, String address, String email, String phone, String rawPassword, Role role) {
		User user = new User();
		user.setFullName(fullName);
		user.setAddress(address);
		user.setEmail(email);
		user.setPhone(phone);
		user.setPassword(passwordEncoder.encode(rawPassword));
		user.setRole(role);
		return user;
	}
}
