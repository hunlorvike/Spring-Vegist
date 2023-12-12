package project.vegist.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import project.vegist.dtos.UserDTO;
import project.vegist.entities.Role;
import project.vegist.entities.User;
import project.vegist.entities.UserRole;
import project.vegist.models.CustomUserDetail;
import project.vegist.models.UserModel;
import project.vegist.repositories.RoleRepository;
import project.vegist.repositories.UserRepository;
import project.vegist.requests.LoginRequest;
import project.vegist.requests.RegisterRequest;
import project.vegist.services.impls.CrudService;
import project.vegist.utils.DateTimeUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService implements CrudService<User, UserDTO, UserModel> {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Autowired
    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, JwtService jwtService, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    // login
    public String login(LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            return jwtService.generateToken((CustomUserDetail) userDetails);
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Invalid email or password", e);
        }
    }

    // register
    public boolean register(RegisterRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        User user = new User(registerRequest.getName(), registerRequest.getEmail(), passwordEncoder.encode(registerRequest.getPassword()));
        addRoleToUser(user, "USER");
        userRepository.save(user);
        return true;
    }

    private void addRoleToUser(User user, String roleName) {
        Optional<Role> userRole = roleRepository.findByRoleName(roleName);

        if (userRole.isEmpty()) {
            Role newUserRole = new Role();
            newUserRole.setRoleName(roleName);
            roleRepository.save(newUserRole);
            userRole = Optional.of(newUserRole);
        }

        boolean hasUserRole = user.getUserRoles().stream()
                .anyMatch(userRoleEntity -> userRoleEntity.getRole().getRoleName().equals(roleName));

        if (!hasUserRole) {
            UserRole userRoleRegister = new UserRole();
            userRoleRegister.setUser(user);
            userRoleRegister.setRole(userRole.get());
            user.getUserRoles().add(userRoleRegister);
        }
    }

    @Override
    public List<UserModel> findAll() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(this::convertToModel)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserModel> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> userPage = userRepository.findAll(pageable);
        return userPage.getContent().stream()
                .map(this::convertToModel)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<UserModel> findById(Long id) {
        Optional<User> userOptional = userRepository.findById(id);
        return userOptional.map(this::convertToModel);
    }

    @Override
    public Optional<UserModel> create(UserDTO userDTO) {
        User newUser = new User();
        convertToEntity(userDTO, newUser);

        User savedUser = userRepository.save(newUser);
        return Optional.ofNullable(convertToModel(savedUser));
    }

    @Override
    public List<UserModel> createAll(List<UserDTO> userDTOS) {
        List<User> savedUsers = userDTOS.stream()
                .map(dto -> {
                    User user = new User();
                    convertToEntity(dto, user);
                    return user;
                })
                .collect(Collectors.toList());

        return userRepository.saveAll(savedUsers).stream()
                .map(this::convertToModel)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<UserModel> update(Long id, UserDTO userDTO) {
        return userRepository.findById(id)
                .map(existingUser -> {
                    convertToEntity(userDTO, existingUser);
                    User updatedUser = userRepository.save(existingUser);
                    return convertToModel(updatedUser);
                });
    }

    @Override
    public List<UserModel> updateAll(Map<Long, UserDTO> longUserDTOMap) {
        return longUserDTOMap.entrySet().stream()
                .map(entry -> {
                    Long userId = entry.getKey();
                    UserDTO userDTO = entry.getValue();

                    return userRepository.findById(userId)
                            .map(existingUser -> {
                                convertToEntity(userDTO, existingUser);
                                return userRepository.save(existingUser);
                            })
                            .map(this::convertToModel)
                            .orElse(null);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public boolean deleleById(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    public boolean deleteAll(List<Long> ids) {
        userRepository.deleteAllById(ids);
        return true;
    }

    @Override
    public List<UserModel> search(String keywords) {
        return null;
    }

    @Override
    public UserModel convertToModel(User user) {
        return new UserModel(user.getId(), user.getFullName(), user.getGender(), user.getEmail(), user.getPhone(),
                user.getPassword(), DateTimeUtils.formatLocalDateTime(user.getCreatedAt()), DateTimeUtils.formatLocalDateTime(user.getUpdatedAt()));
    }

    @Override
    public void convertToEntity(UserDTO userDTO, User user) {
        user.setFullName(userDTO.getFullName());
        user.setGender(userDTO.getGender());
        user.setEmail(userDTO.getEmail());
        user.setPhone(userDTO.getPhone());
        user.setPassword(userDTO.getPassword());
    }
}
