package project.vegist.services;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.vegist.dtos.ActionDTO;
import project.vegist.entities.Action;
import project.vegist.models.ActionModel;
import project.vegist.repositories.ActionRepository;
import project.vegist.services.impls.CrudService;
import project.vegist.utils.SpecificationsBuilder;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Service
public class ActionService implements CrudService<Action, ActionDTO, ActionModel> {
    private final ActionRepository actionRepository;

    @Autowired
    public ActionService(ActionRepository actionRepository) {
        this.actionRepository = actionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActionModel> findAll() {
        return actionRepository.findAll().stream()
                .map(this::convertToModel)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActionModel> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return actionRepository.findAll(pageable).getContent().stream()
                .map(this::convertToModel)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ActionModel> findById(Long id) {
        return actionRepository.findById(id).map(this::convertToModel);
    }

    @Override
    @Transactional
    public Optional<ActionModel> create(ActionDTO actionDTO) {
        Action newAction = new Action();
        convertToEntity(actionDTO, newAction);
        return Optional.ofNullable(convertToModel(actionRepository.save(newAction)));
    }

    @Override
    @Transactional
    public List<ActionModel> createAll(List<ActionDTO> actionDTOS) {
        List<Action> newActions = actionDTOS.stream()
                .map(actionDTO -> {
                    Action newAction = new Action();
                    convertToEntity(actionDTO, newAction);
                    return newAction;
                })
                .collect(Collectors.toList());

        return actionRepository.saveAll(newActions).stream()
                .map(this::convertToModel)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Optional<ActionModel> update(Long id, ActionDTO actionDTO) {
        return actionRepository.findById(id)
                .map(existingAction -> {
                    if (actionDTO.getActionName() != null) {
                        existingAction.setActionName(actionDTO.getActionName());
                    }
                    return convertToModel(actionRepository.save(existingAction));
                });
    }

    @Override
    @Transactional
    public List<ActionModel> updateAll(Map<Long, ActionDTO> longActionDTOMap) {
        return longActionDTOMap.entrySet().stream()
                .map(entry -> {
                    Long id = entry.getKey();
                    ActionDTO actionDTO = entry.getValue();
                    return actionRepository.findById(id)
                            .map(existingAction -> {
                                if (actionDTO.getActionName() != null) {
                                    existingAction.setActionName(actionDTO.getActionName());
                                }
                                return existingAction;
                            })
                            .orElse(null);
                })
                .filter(Objects::nonNull)
                .map(actionRepository::save)
                .map(this::convertToModel)
                .collect(Collectors.toList());
    }


    @Override
    @Transactional
    public boolean deleteById(Long id) {
        if (actionRepository.existsById(id)) {
            performDelete(id);
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public boolean deleteAll(List<Long> ids) {
        List<Action> actionsToDelete = actionRepository.findAllById(ids);
        if (!actionsToDelete.isEmpty()) {
            actionRepository.deleteAll(actionsToDelete);
            return true;
        }
        return false;
    }


    @Override
    @Transactional(readOnly = true)
    public List<ActionModel> search(String keywords) {
        SpecificationsBuilder<Action> specificationsBuilder = new SpecificationsBuilder<>();

        if (!StringUtils.isEmpty(keywords)) {
            specificationsBuilder
                    .like("actionName", keywords) // Add more fields if needed
                    .or(builder -> {
                        // Add additional search conditions if needed
                        // builder.like("anotherField", keywords);
                    });
        }

        return actionRepository.findAll(specificationsBuilder.build()).stream()
                .map(this::convertToModel)
                .collect(Collectors.toList());
    }

    public List<ActionModel> search(String keywords, Consumer<SpecificationsBuilder<Action>> additionalConditions) {
        SpecificationsBuilder<Action> specificationsBuilder = new SpecificationsBuilder<>();

        specificationsBuilder.like("actionName", keywords);

        if (additionalConditions != null) {
            additionalConditions.accept(specificationsBuilder);
        }

        return actionRepository.findAll(specificationsBuilder.build()).stream()
                .map((Action action) -> convertToModel(action))
                .collect(Collectors.toList());
    }


    @Override
    public ActionModel convertToModel(Action action) {
        return new ActionModel(action.getId(), action.getActionName());
    }

    @Override
    public void convertToEntity(ActionDTO actionDTO, Action action) {
        action.setActionName(actionDTO.getActionName());
    }

    private void performDelete(Long id) {
        actionRepository.deleteById(id);
    }
}
