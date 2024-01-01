package project.vegist.services;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import project.vegist.utils.SpecificationsBuilder;

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

    public String login(LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String accessToken = jwtService.generateToken((CustomUserDetail) userDetails);

            // Check if the user has a refresh token
            User user = userRepository.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + loginRequest.getEmail()));

            if (StringUtils.isNotBlank(user.getRefreshToken())) {
                String refreshedAccessToken = jwtService.refreshAccessToken(user.getRefreshToken());
                if (StringUtils.isNotBlank(refreshedAccessToken)) {
                    return refreshedAccessToken;
                }
            }

            return accessToken;
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

        user.setUserRoles(new ArrayList<>());

        addRoleToUser(user);
        userRepository.save(user);

        // Generate a refresh token for the new user
        String refreshToken = jwtService.generateRefreshToken(user);
        user.setRefreshToken(refreshToken);
        userRepository.save(user);

        return true;
    }

    private void addRoleToUser(User user) {
        List<UserRole> userRoles = Optional.ofNullable(user.getUserRoles()).orElseGet(ArrayList::new);

        Optional<Role> userRole = roleRepository.findByRoleName("USER");

        if (userRole.isEmpty()) {
            Role newUserRole = new Role();
            newUserRole.setRoleName("USER");
            roleRepository.save(newUserRole);
            userRole = Optional.of(newUserRole);
        }

        boolean hasUserRole = userRoles.stream()
                .anyMatch(userRoleEntity -> userRoleEntity.getRole().getRoleName().equals("USER"));

        if (!hasUserRole) {
            UserRole userRoleRegister = new UserRole();
            userRoleRegister.setUser(user);
            userRoleRegister.setRole(userRole.get());
            userRoles.add(userRoleRegister);
            user.setUserRoles(userRoles);
        }
    }


    @Override
    @Transactional(readOnly = true)
    public List<UserModel> findAll() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(this::convertToModel)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserModel> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> userPage = userRepository.findAll(pageable);
        return userPage.getContent().stream()
                .map(this::convertToModel)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserModel> findById(Long id) {
        Optional<User> userOptional = userRepository.findById(id);
        return userOptional.map(this::convertToModel);
    }

    @Override
    @Transactional
    public Optional<UserModel> create(UserDTO userDTO) {
        User newUser = new User();
        convertToEntity(userDTO, newUser);

        User savedUser = userRepository.save(newUser);
        return Optional.ofNullable(convertToModel(savedUser));
    }

    @Override
    @Transactional
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
    @Transactional
    public Optional<UserModel> update(Long id, UserDTO userDTO) {
        return userRepository.findById(id)
                .map(existingUser -> {
                    convertToEntity(userDTO, existingUser);
                    User updatedUser = userRepository.save(existingUser);
                    return convertToModel(updatedUser);
                });
    }

    @Override
    @Transactional
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
    @Transactional
    public boolean deleteById(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public boolean deleteAll(List<Long> ids) {
        userRepository.deleteAllById(ids);
        return true;
    }


    @Override
    @Transactional(readOnly = true)
    public List<UserModel> search(String keywords) {
        SpecificationsBuilder<User> specificationsBuilder = new SpecificationsBuilder<>();

        if (!StringUtils.isEmpty(keywords)) {
            specificationsBuilder
                    .or(builder -> {
                        builder.like("fullName", keywords);
                        builder.like("email", keywords);
                        // Add additional search conditions if needed
                        // builder.like("anotherField", keywords);
                    });
        }

        Specification<User> spec = specificationsBuilder.build();

        List<UserModel> result = userRepository.findAll(spec).stream()
                .map(this::convertToModel)
                .collect(Collectors.toList());

        return result;
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
