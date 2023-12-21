package project.vegist.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.vegist.dtos.UserActionDTO;
import project.vegist.entities.UserAction;
import project.vegist.models.UserActionModel;
import project.vegist.repositories.ActionRepository;
import project.vegist.repositories.UserActionRepository;
import project.vegist.repositories.UserRepository;
import project.vegist.services.impls.CrudService;
import project.vegist.utils.DateTimeUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserActionService implements CrudService<UserAction, UserActionDTO, UserActionModel> {
    private final UserRepository userRepository;
    private final ActionRepository actionRepository;
    private final UserActionRepository userActionRepository;

    @Autowired
    public UserActionService(UserRepository userRepository, ActionRepository actionRepository, UserActionRepository userActionRepository) {
        this.userRepository = userRepository;
        this.actionRepository = actionRepository;
        this.userActionRepository = userActionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserActionModel> findAll() {
        return userActionRepository.findAll().stream()
                .map(this::convertToModel)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserActionModel> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return userActionRepository.findAll(pageable).getContent().stream()
                .map(this::convertToModel)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserActionModel> findById(Long id) {
        return userActionRepository.findById(id).map(this::convertToModel);
    }

    @Override
    @Transactional
    public Optional<UserActionModel> create(UserActionDTO userActionDTO) {
        return userRepository.findById(userActionDTO.getUserId())
                .flatMap(user -> actionRepository.findById(userActionDTO.getActionId())
                        .map(action -> {
                            UserAction newUserAction = new UserAction(user, action);
                            UserAction savedUserAction = userActionRepository.save(newUserAction);
                            return convertToModel(savedUserAction);
                        })
                );
    }

    @Override
    @Transactional
    public List<UserActionModel> createAll(List<UserActionDTO> userActionDTOS) {
        List<UserAction> newUserActions = userActionDTOS.stream()
                .map(userActionDTO -> {
                    UserAction newUserAction = new UserAction();
                    convertToEntity(userActionDTO, newUserAction);
                    return newUserAction;
                })
                .toList();

        List<UserAction> savedUserActions = userActionRepository.saveAll(newUserActions);
        return savedUserActions.stream()
                .map(this::convertToModel)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Optional<UserActionModel> update(Long id, UserActionDTO userActionDTO) {
        return userActionRepository.findById(id)
                .map(existingUserAction -> {
                    convertToEntity(userActionDTO, existingUserAction);
                    UserAction updatedUserAction = userActionRepository.save(existingUserAction);
                    return convertToModel(updatedUserAction);
                });
    }

    @Override
    @Transactional
    public List<UserActionModel> updateAll(Map<Long, UserActionDTO> longUserActionDTOMap) {
        List<UserAction> updatedUserActions = longUserActionDTOMap.entrySet().stream()
                .map(entry -> {
                    Long id = entry.getKey();
                    UserActionDTO userActionDTO = entry.getValue();

                    return userActionRepository.findById(id)
                            .map(existingUserAction -> {
                                convertToEntity(userActionDTO, existingUserAction);
                                return existingUserAction;
                            })
                            .orElse(null);
                })
                .filter(Objects::nonNull)
                .toList();

        List<UserAction> savedUserActions = userActionRepository.saveAll(updatedUserActions);
        return savedUserActions.stream()
                .map(this::convertToModel)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean deleteById(Long id) {
        if (userActionRepository.existsById(id)) {
            userActionRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public boolean deleteAll(List<Long> ids) {
        List<UserAction> userActionsToDelete = userActionRepository.findAllById(ids);
        if (!userActionsToDelete.isEmpty()) {
            userActionRepository.deleteAll(userActionsToDelete);
            return true;
        }
        return false;
    }

    @Override
    public List<UserActionModel> search(String keywords) {
        // Implement your search logic
        return null;
    }

    @Override
    public UserActionModel convertToModel(UserAction userAction) {
        return new UserActionModel(
                userAction.getId(),
                userAction.getUser().getId(),
                userAction.getAction().getId(),
                DateTimeUtils.formatLocalDateTime(userAction.getCreatedAt()),
                DateTimeUtils.formatLocalDateTime(userAction.getUpdatedAt())
        );
    }

    @Override
    public void convertToEntity(UserActionDTO userActionDTO, UserAction userAction) {
        userRepository.findById(userActionDTO.getUserId())
                .ifPresent(userAction::setUser);

        actionRepository.findById(userActionDTO.getActionId())
                .ifPresent(userAction::setAction);
    }
}
