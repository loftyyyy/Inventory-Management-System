package com.rho.ims.service;

import com.rho.ims.api.exception.DuplicateCredentialException;
import com.rho.ims.dto.LoginDTO;
import com.rho.ims.dto.SignupDTO;
import com.rho.ims.dto.UpdateUserDTO;
import com.rho.ims.dto.UserResponseDTO;
import com.rho.ims.model.Role;
import com.rho.ims.model.User;
import com.rho.ims.respository.RoleRepository;
import com.rho.ims.respository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
    }

    public User createUser(SignupDTO signupDTO) {
        if (userRepository.existsByUsername(signupDTO.getUsername())) {
            throw new DuplicateCredentialException("username", signupDTO.getUsername());
        }
        if (userRepository.existsByEmail(signupDTO.getEmail())) {
            throw new DuplicateCredentialException("email", signupDTO.getEmail());
        }


        Role role = roleRepository.findById(signupDTO.getRoleId()).orElseThrow(() -> new RuntimeException("Role not found"));
        User user = new User();
        user.setUsername(signupDTO.getUsername());
        user.setPassword(signupDTO.getPassword());
        user.setEmail(signupDTO.getEmail());
        user.setRole(role);
        String hashedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(hashedPassword);
        return userRepository.save(user);
    }

    public User loginUser(LoginDTO loginDTO) {
        User existingUser = userRepository.findByUsernameOrEmail(loginDTO.getUsername(), loginDTO.getUsername());
        if (existingUser == null || !passwordEncoder.matches(loginDTO.getPassword(), existingUser.getPassword())) {
            throw new RuntimeException("Invalid username or password");
        }
        return existingUser;
    }

    public User findById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
    }

    public List<UserResponseDTO> getAllUsers(){
        List<User> users = userRepository.findAll();
        return users.stream().map(user -> new UserResponseDTO(user)).toList();

    }

    public User updateUser(Long id, UpdateUserDTO updateUserDTO) {
        User existingUser = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));

        if (updateUserDTO.getUsername() != null) {
            existingUser.setUsername(updateUserDTO.getUsername());
        }
        if (updateUserDTO.getEmail() != null) {
            existingUser.setEmail(updateUserDTO.getEmail());
        }
        if (updateUserDTO.getPassword() != null) {
            existingUser.setPassword(passwordEncoder.encode(updateUserDTO.getPassword()));
        }

        //TODO: Future enhancement, implement the updatedBy field

        return userRepository.save(existingUser);
    }

    public void deleteUser(Long id) {
        User existingUser = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        userRepository.delete(existingUser);
    }

}
