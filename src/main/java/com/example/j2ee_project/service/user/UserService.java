package com.example.j2ee_project.service.user;

import com.example.j2ee_project.model.dto.UserDTO;
import com.example.j2ee_project.model.request.user.UserRequest;
import com.example.j2ee_project.entity.Status;
import com.example.j2ee_project.entity.User;
import com.example.j2ee_project.exception.DuplicateResourceException;
import com.example.j2ee_project.exception.ResourceNotFoundException;
import com.example.j2ee_project.repository.UserRepository;
import com.example.j2ee_project.repository.StatusRepository;
import com.example.j2ee_project.service.role.RoleService;
import com.example.j2ee_project.utils._enum.EStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final RoleService roleService;
    private final StatusRepository statusRepository;

    @Autowired
    public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder, RoleService roleService,
            StatusRepository statusRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleService = roleService;
        this.statusRepository = statusRepository;
    }

    public UserDTO createUser(UserRequest userRequest) {
        if (userRepository.existsByUsername(userRequest.getUsername())) {
            throw new DuplicateResourceException("Tên đăng nhập đã tồn tại");
        }
        if (userRepository.existsByEmail(userRequest.getEmail())) {
            throw new DuplicateResourceException("Email đã tồn tại");
        }

        if (userRepository.existsByPhoneNumber(userRequest.getPhoneNumber())) {
            throw new DuplicateResourceException("Số điện thoại đã tồn tại");
        }

        Status status = statusRepository.findById(userRequest.getStatusId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy trạng thái với ID: " + userRequest.getStatusId()));

        User user = new User();
        user.setUsername(userRequest.getUsername());
        user.setEmail(userRequest.getEmail());
        user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        user.setFullName(userRequest.getFullName());
        user.setPhoneNumber(userRequest.getPhoneNumber());
        user.setJoinDate(LocalDateTime.now());
        user.setTotalSpent(java.math.BigDecimal.valueOf(0.0));
        user.setLoyaltyPoints(0);
        user.setStatusWork(userRequest.getStatusWork());
        user.setStatus(status);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        // Tạo mật khẩu random và gửi email
//        String randomPassword = UUID.randomUUID().toString().substring(0, 8); // Mật khẩu random 8 ký tự
//        // Hash password and save
//        user.setPassword(/* hash randomPassword */);
//
//        User saved = userRepository.save(user);
//
//        // Gửi email mật khẩu random
//        emailService.sendRandomPassword(saved, randomPassword);
//
//        // Gửi mã xác nhận
//        emailService.sendVerificationCode(saved);

        // Tạo và gửi voucher chào mừng
//        String voucherCode = voucherService.generateUniqueVoucherForUser(saved.getUserID());
//        voucherService.sendVoucherToUser(saved.getUserID(), voucherCode);

        Integer roleId = userRequest.getRoleId();
        if (roleId == null) {
            // Tạm thời set như vậy
            try {
                // roleId = roleService.getDefaultRole().getId();
                roleId = 1; // default USER role
            } catch (Exception e) {
                roleId = 1; // fallback USER
            }
        }
        user.setRoleId(roleId);

        if (userRequest.getJoinDate() != null) {
            user.setJoinDate(userRequest.getJoinDate());
        } else {
            user.setJoinDate(LocalDateTime.now());
        }

        user = userRepository.save(user);
        return mapToUserDTO(user);
    }

    public List<UserDTO> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(this::mapToUserDTO)
                .collect(Collectors.toList());
    }

    // Xử lý OAuth2 user (Google hoặc Facebook)
    public User processOAuthUser(OAuth2User oAuth2User, String provider) {
        String emailAttribute = oAuth2User.getAttribute("email");
        final String name = oAuth2User.getAttribute("name");
        final String providerId = provider.equals("Facebook") ? oAuth2User.getAttribute("id") :
                provider.equals("Google") ? oAuth2User.getAttribute("sub") : null;

        // Kiểm tra email null
        final String email;
        if (emailAttribute == null) {
            throw new RuntimeException("Email không khả dụng. Vui lòng xác minh email trong tài khoản " + provider + " của bạn.");
        } else {
            email = emailAttribute;
        }

        // Kiểm tra email xác minh cho Google
        if (provider.equals("Google")) {
            Boolean emailVerified = oAuth2User.getAttribute("email_verified");
            if (emailVerified == null || !emailVerified) {
                throw new RuntimeException("Email chưa được xác minh. Vui lòng xác minh email trong tài khoản Google của bạn.");
            }
        }

        // Log để debug
        System.out.println("Processing " + provider + " user with email: " + email);
        System.out.println(provider + " attributes: " + oAuth2User.getAttributes());

        return userRepository.findByEmail(email)
                .map(user -> {
                    // Kiểm tra trạng thái INACTIVE
                    if (EStatus.INACTIVE.equals(user.getStatus().getStatusName())) {
                        throw new DisabledException("Tài khoản đã bị vô hiệu hóa");
                    }
                    return user;
                })
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setEmail(email);
                    newUser.setUsername(email); // Dùng email làm username
                    newUser.setFullName(name != null ? name : provider + " User");
                    newUser.setPassword(null); // Không cần password cho Google, Facebook user
                    newUser.setJoinDate(LocalDateTime.now());
                    newUser.setTotalSpent(BigDecimal.valueOf(0.0));
                    newUser.setLoyaltyPoints(0);
                    newUser.setStatusWork(null);
                    newUser.setCreatedAt(LocalDateTime.now());
                    newUser.setUpdatedAt(LocalDateTime.now());
                    newUser.setRoleId(1); // Default USER role
                    newUser.setStatus(statusRepository.findById(EStatus.UNVERIFIED.getCode())
                            .orElseThrow(() -> new RuntimeException("Không tìm thấy status Unverified")));
                    return userRepository.save(newUser);
                });
    }

    // Hàm lấy danh sách người dùng kèm phân trang và lọc
    public Map<String, Object> getUsersPaginated(int offset, int limit,
            String username, String email,
            String status, Integer roleId) {
        Pageable pageable = PageRequest.of(offset / limit, limit);
        Page<User> page = userRepository.findUsersFiltered(username, email, status, roleId, pageable);

        List<UserDTO> users = page.getContent().stream()
                .map(this::mapToUserDTO)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("users", users);
        response.put("currentPage", page.getNumber());
        response.put("totalItems", page.getTotalElements());
        response.put("totalPages", page.getTotalPages());
        return response;
    }

    public UserDTO getUserById(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + userId));
        return mapToUserDTO(user);
    }

    public UserDTO getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy người dùng với tên đăng nhập: " + username));
        return mapToUserDTO(user);
    }

    public UserDTO updateUser(Integer userId, UserDTO userDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + userId));

        Status status = statusRepository.findById(userDTO.getStatusId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy trạng thái với ID: " + userDTO.getStatusId()));

        if (userDTO.getUsername() != null && !userDTO.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsername(userDTO.getUsername())) {
                throw new DuplicateResourceException("Tên đăng nhập đã tồn tại");
            }
            user.setUsername(userDTO.getUsername());
        }

        if (userDTO.getEmail() != null && !userDTO.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(userDTO.getEmail())) {
                throw new DuplicateResourceException("Email đã tồn tại");
            }
            user.setEmail(userDTO.getEmail());
        }

        if (userDTO.getFullName() != null)
            user.setFullName(userDTO.getFullName());
        if (userDTO.getPhoneNumber() != null)
            user.setPhoneNumber(userDTO.getPhoneNumber());
        if (userDTO.getRoleId() != null)
            user.setRoleId(userDTO.getRoleId());
        if (userDTO.getStatusId() != null)
            user.setStatus(status);
        if (userDTO.getStatusWork() != null)
            user.setStatusWork(userDTO.getStatusWork());
        if (userDTO.getTotalSpent() != null)
            user.setTotalSpent(userDTO.getTotalSpent());
        if (userDTO.getLoyaltyPoints() != null)
            user.setLoyaltyPoints(userDTO.getLoyaltyPoints());

        user.setUpdatedAt(LocalDateTime.now());

        user = userRepository.save(user);
        return mapToUserDTO(user);
    }

    public void deleteUser(Integer userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + userId);
        }
        userRepository.deleteById(userId);
    }

    private UserDTO mapToUserDTO(User user) {
        UserDTO.UserDTOBuilder builder = UserDTO.builder()
                .userId(user.getUserID())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .roleId(user.getRoleId())
                .statusId(user.getStatus().getStatusID())
                .statusWork(user.getStatusWork())
                .totalSpent(user.getTotalSpent())
                .loyaltyPoints(user.getLoyaltyPoints())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt());
        return builder.build();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng: " + username));

        // Nếu user có status = "Inactive" thì disable
        if (EStatus.INACTIVE.equals(user.getStatus())) {
            throw new DisabledException("Tài khoản đã bị vô hiệu hóa");
        }

        Set<GrantedAuthority> authorities = new HashSet<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

        try {
            if (user.getRoleId() != null) {
                String roleName = roleService.getRoleNameByRoleId(user.getRoleId());
                if (roleName != null && !roleName.equals("UNKNOWN") && !roleName.equals("ERROR")) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + roleName));
                }
            }
        } catch (Exception e) {
            System.out.println("Warning: Could not load role for user " + username);
        }

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(EStatus.INACTIVE.equals(user.getStatus().getStatusName()))
                .build();
    }
}
