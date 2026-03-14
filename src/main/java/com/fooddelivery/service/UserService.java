package com.fooddelivery.service;

import com.fooddelivery.dto.AddressDto;
import com.fooddelivery.dto.UserDto;
import com.fooddelivery.entity.Address;
import com.fooddelivery.entity.User;
import com.fooddelivery.exception.BadRequestException;
import com.fooddelivery.exception.ResourceNotFoundException;
import com.fooddelivery.exception.UnauthorizedException;
import com.fooddelivery.repository.AddressRepository;
import com.fooddelivery.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, AddressRepository addressRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserDto.ProfileResponse getProfile(String email) {
        return toProfile(findByEmail(email));
    }

    @Transactional
    public UserDto.ProfileResponse updateProfile(String email, UserDto.UpdateProfileRequest req) {
        User user = findByEmail(email);
        if (req.getName()    != null) user.setName(req.getName());
        if (req.getPhone()   != null) user.setPhone(req.getPhone());
        if (req.getAddress() != null) user.setAddress(req.getAddress());
        return toProfile(userRepository.save(user));
    }

    @Transactional
    public void changePassword(String email, UserDto.ChangePasswordRequest req) {
        User user = findByEmail(email);
        if (!passwordEncoder.matches(req.getCurrentPassword(), user.getPassword()))
            throw new BadRequestException("Current password is incorrect");
        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);
    }

    // ── Addresses ─────────────────────────────────────────────────────────────

    public List<AddressDto.AddressResponse> getAddresses(String email) {
        User user = findByEmail(email);
        return addressRepository.findByUserIdOrderByDefaultAddressDesc(user.getId())
                .stream().map(this::toAddressResponse).collect(Collectors.toList());
    }

    @Transactional
    public AddressDto.AddressResponse addAddress(String email, AddressDto.CreateRequest req) {
        User user = findByEmail(email);
        if (req.isDefaultAddress()) clearDefault(user.getId());
        Address address = Address.builder()
                .user(user).label(req.getLabel()).fullAddress(req.getFullAddress())
                .city(req.getCity()).pincode(req.getPincode())
                .latitude(req.getLatitude()).longitude(req.getLongitude())
                .defaultAddress(req.isDefaultAddress())
                .build();
        return toAddressResponse(addressRepository.save(address));
    }

    @Transactional
    public AddressDto.AddressResponse setDefaultAddress(String email, Long addressId) {
        User user = findByEmail(email);
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));
        if (!address.getUser().getId().equals(user.getId()))
            throw new UnauthorizedException("Address does not belong to you");
        clearDefault(user.getId());
        address.setDefaultAddress(true);
        return toAddressResponse(addressRepository.save(address));
    }

    @Transactional
    public void deleteAddress(String email, Long addressId) {
        User user = findByEmail(email);
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));
        if (!address.getUser().getId().equals(user.getId()))
            throw new UnauthorizedException("Address does not belong to you");
        addressRepository.delete(address);
    }

    private void clearDefault(Long userId) {
        addressRepository.findByUserIdAndDefaultAddressTrue(userId)
                .ifPresent(a -> { a.setDefaultAddress(false); addressRepository.save(a); });
    }

    private User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    private UserDto.ProfileResponse toProfile(User u) {
        return UserDto.ProfileResponse.builder()
                .id(u.getId()).name(u.getName()).email(u.getEmail())
                .phone(u.getPhone()).address(u.getAddress())
                .role(u.getRole()).active(u.isActive()).build();
    }

    private AddressDto.AddressResponse toAddressResponse(Address a) {
        return AddressDto.AddressResponse.builder()
                .id(a.getId()).label(a.getLabel()).fullAddress(a.getFullAddress())
                .city(a.getCity()).pincode(a.getPincode())
                .latitude(a.getLatitude()).longitude(a.getLongitude())
                .defaultAddress(a.isDefaultAddress()).build();
    }
}
