package project.vegist.services;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.vegist.dtos.RoleDTO;
import project.vegist.entities.Role;
import project.vegist.models.RoleModel;
import project.vegist.repositories.RoleRepository;
import project.vegist.services.impls.CrudService;
import project.vegist.utils.SpecificationsBuilder;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RoleService implements CrudService<Role, RoleDTO, RoleModel> {
    private final RoleRepository roleRepository;

    @Autowired
    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleModel> findAll() {
        return roleRepository.findAll()
                .stream().map(this::convertToModel)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleModel> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return roleRepository.findAll(pageable).getContent().stream()
                .map(this::convertToModel)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RoleModel> findById(Long id) {
        return roleRepository.findById(id).map(this::convertToModel);
    }

    @Override
    @Transactional
    public Optional<RoleModel> create(RoleDTO roleDTO) {
        Role newRole = new Role();
        convertToEntity(roleDTO, newRole);
        Role savedRole = roleRepository.save(newRole);
        return Optional.ofNullable(convertToModel(savedRole));
    }

    @Override
    @Transactional
    public List<RoleModel> createAll(List<RoleDTO> roleDTOS) {
        List<Role> newRoles = roleDTOS.stream()
                .map(roleDTO -> {
                    Role newRole = new Role();
                    convertToEntity(roleDTO, newRole);
                    return newRole;
                })
                .collect(Collectors.toList());

        List<Role> savedRoles = roleRepository.saveAll(newRoles);
        return savedRoles.stream()
                .map(this::convertToModel)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Optional<RoleModel> update(Long id, RoleDTO roleDTO) {
        return roleRepository.findById(id)
                .map(existingRole -> {
                    convertToEntity(roleDTO, existingRole);
                    Role updatedRole = roleRepository.save(existingRole);
                    return convertToModel(updatedRole);
                });
    }

    @Override
    @Transactional
    public List<RoleModel> updateAll(Map<Long, RoleDTO> longRoleDTOMap) {
        return longRoleDTOMap.entrySet().stream()
                .map(entry -> update(entry.getKey(), entry.getValue()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean deleteById(Long id) {
        if (roleRepository.existsById(id)) {
            roleRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public boolean deleteAll(List<Long> ids) {
        List<Role> rolesToDelete = roleRepository.findAllById(ids);
        roleRepository.deleteAll(rolesToDelete);
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleModel> search(String keywords) {
        SpecificationsBuilder<Role> specificationsBuilder = new SpecificationsBuilder<>();

        if (!StringUtils.isEmpty(keywords)) {
            specificationsBuilder
                    .or(builder -> builder.like("roleName", keywords));
            // Add additional search conditions if needed
            // builder.like("anotherField", keywords);
        }

        Specification<Role> spec = specificationsBuilder.build();

        return roleRepository.findAll(spec).stream()
                .map(this::convertToModel)
                .collect(Collectors.toList());
    }

    @Override
    public RoleModel convertToModel(Role role) {
        if (role == null) {
            return null;
        }
        return new RoleModel(role.getId(), role.getRoleName());
    }

    @Override
    public void convertToEntity(RoleDTO roleDTO, Role role) {
        if (roleDTO != null) {
            role.setRoleName(roleDTO.getRoleName());
        }
    }
}
