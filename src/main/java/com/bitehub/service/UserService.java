package com.bitehub.service;

import com.bitehub.dto.AddressDTO;
import com.bitehub.dto.UserDTO;
import com.bitehub.exception.BadRequestException;
import com.bitehub.exception.ResourceNotFoundException;
import com.bitehub.model.Address;
import com.bitehub.model.Role;
import com.bitehub.model.User;
import com.bitehub.repository.AddressRepository;
import com.bitehub.repository.UserRepository;
import com.bitehub.dto.RegisterRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       AddressRepository addressRepository,
                       AuthService authService,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
        this.authService = authService;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream().map(UserDTO::new).collect(Collectors.toList());
    }

    public List<UserDTO> getUsersByRole(Role role) {
        return userRepository.findByRole(role).stream().map(UserDTO::new).collect(Collectors.toList());
    }

    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return new UserDTO(user);
    }

    @Transactional
    public UserDTO toggleUserStatus(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        user.setEnabled(!user.isEnabled());
        return new UserDTO(userRepository.save(user));
    }

    public UserDTO getCurrentUserProfile() {
        return new UserDTO(authService.getCurrentUser());
    }

    @Transactional
    public UserDTO updateUserProfile(UserDTO dto) {
        User user = authService.getCurrentUser();
        if (dto.getName() != null) user.setName(dto.getName());
        if (dto.getPhone() != null) user.setPhone(dto.getPhone());
        if (dto.getProfileImageUrl() != null) user.setProfileImageUrl(dto.getProfileImageUrl());
        return new UserDTO(userRepository.save(user));
    }

    public List<AddressDTO> getMyAddresses() {
        User user = authService.getCurrentUser();
        return addressRepository.findByUserId(user.getId()).stream()
                .map(AddressDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public AddressDTO addAddress(AddressDTO dto) {
        User user = authService.getCurrentUser();
        Address address = new Address();
        address.setUser(user);
        address.setLabel(dto.getLabel() != null ? dto.getLabel() : "Home");
        address.setStreetAddress(dto.getStreetAddress());
        address.setCity(dto.getCity() != null ? dto.getCity() : "City");
        address.setPostalCode(dto.getPostalCode());
        address.setContactPhone(dto.getContactPhone() != null ? dto.getContactPhone() : user.getPhone());
        address.setDefault(dto.isDefault());

        return new AddressDTO(addressRepository.save(address));
    }

    @Transactional
    public void deleteAddress(Long id) {
        User user = authService.getCurrentUser();
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + id));

        if (!address.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("You can only delete your own addresses");
        }

        addressRepository.delete(address);
    }

    @Transactional
    public UserDTO createAdminUser(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already registered: " + request.getEmail());
        }
        User admin = new User(
                request.getName(),
                request.getEmail().toLowerCase().trim(),
                passwordEncoder.encode(request.getPassword()),
                request.getPhone() != null && !request.getPhone().isBlank() ? request.getPhone() : "+1 555-0100",
                Role.ROLE_ADMIN
        );
        admin.setEnabled(true);
        return new UserDTO(userRepository.save(admin));
    }

    @Transactional
    public UserDTO promoteUserToAdmin(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        user.setRole(Role.ROLE_ADMIN);
        return new UserDTO(userRepository.save(user));
    }
}
