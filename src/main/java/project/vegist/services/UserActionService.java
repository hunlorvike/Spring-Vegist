package project.vegist.services;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.vegist.dtos.UserActionDTO;
import project.vegist.entities.Action;
import project.vegist.entities.User;
import project.vegist.entities.UserAction;
import project.vegist.models.UserActionModel;
import project.vegist.repositories.ActionRepository;
import project.vegist.repositories.UserActionRepository;
import project.vegist.repositories.UserRepository;
import project.vegist.services.impls.CrudService;
import project.vegist.utils.DateTimeUtils;
import project.vegist.utils.SpecificationsBuilder;

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
        Optional<User> optionalUser = userRepository.findById(userActionDTO.getUserId());
        Optional<Action> optionalAction = actionRepository.findById(userActionDTO.getActionId());

        if (optionalUser.isPresent() && optionalAction.isPresent()) {
            User user = optionalUser.get();
            Action action = optionalAction.get();

            UserAction newUserAction = new UserAction(user, action);
            UserAction savedUserAction = userActionRepository.save(newUserAction);
            return Optional.ofNullable(convertToModel(savedUserAction));
        }

        return Optional.empty();
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
    @Transactional(readOnly = true)
    public List<UserActionModel> search(String keywords) {
        SpecificationsBuilder<UserAction> specificationsBuilder = new SpecificationsBuilder<>();

        if (!StringUtils.isEmpty(keywords)) {
            specificationsBuilder
                    .or(builder -> {
                        builder.like("user.username", keywords); // assuming you have a 'username' field in the 'User' entity
                        builder.like("action.actionName", keywords); // assuming you have a 'actionName' field in the 'Action' entity
                        // Add additional search conditions if needed
                        // builder.like("anotherField", keywords);
                    });
        }

        Specification<UserAction> spec = specificationsBuilder.build();

        return userActionRepository.findAll(spec).stream()
                .map(this::convertToModel)
                .collect(Collectors.toList());
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
