package project.vegist.services;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.vegist.dtos.AddressDTO;
import project.vegist.entities.Address;
import project.vegist.entities.User;
import project.vegist.exceptions.ResourceNotFoundException;
import project.vegist.models.AddressModel;
import project.vegist.repositories.AddressRepository;
import project.vegist.repositories.UserRepository;
import project.vegist.services.impls.CrudService;
import project.vegist.utils.DateTimeUtils;
import project.vegist.utils.SpecificationsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AddressService implements CrudService<Address, AddressDTO, AddressModel> {
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    @Autowired
    public AddressService(AddressRepository addressRepository, UserRepository userRepository) {
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AddressModel> findAll() {
        List<Address> addresses = addressRepository.findAll();
        return addresses.stream()
                .map(this::convertToModel)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AddressModel> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Address> addressPage = addressRepository.findAll(pageable);
        return addressPage.getContent().stream()
                .map(this::convertToModel)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AddressModel> findById(Long id) {
        Optional<Address> addressOptional = addressRepository.findById(id);
        return addressOptional.map(this::convertToModel);
    }

    @Override
    @Transactional
    public Optional<AddressModel> create(AddressDTO addressDTO) {
        if (!userRepository.existsById(addressDTO.getUserId())) {
            throw new ResourceNotFoundException("User", addressDTO.getUserId(), HttpStatus.NOT_FOUND);
        }

        Address newAddress = new Address();
        convertToEntity(addressDTO, newAddress);

        Address savedAddress = addressRepository.save(newAddress);
        return Optional.ofNullable(convertToModel(savedAddress));
    }

    @Override
    @Transactional
    public List<AddressModel> createAll(List<AddressDTO> addressDTOS) {
        List<Address> addresses = addressDTOS.stream()
                .map(dto -> {
                    Address address = new Address();
                    convertToEntity(dto, address);
                    return address;
                })
                .toList();
        List<Address> savedAddresses = addressRepository.saveAll(addresses);
        return savedAddresses.stream()
                .map(this::convertToModel)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Optional<AddressModel> update(Long id, AddressDTO addressDTO) {
        if (!addressRepository.existsById(id)) {
            throw new ResourceNotFoundException("Address", id, HttpStatus.NOT_FOUND);
        }

        return addressRepository.findById(id)
                .map(existingAddress -> {
                    if (addressDTO != null) {
                        convertToEntity(addressDTO, existingAddress);
                        Address updatedAddress = addressRepository.save(existingAddress);
                        return convertToModel(updatedAddress);
                    } else {
                        throw new IllegalArgumentException("AddressDTO is null");
                    }
                });
    }


    @Override
    @Transactional
    public List<AddressModel> updateAll(Map<Long, AddressDTO> longAddressDTOMap) {
        List<AddressModel> updatedAddressModels = new ArrayList<>();
        for (Map.Entry<Long, AddressDTO> entry : longAddressDTOMap.entrySet()) {
            Long addressId = entry.getKey();
            AddressDTO addressDTO = entry.getValue();

            Optional<Address> optionalAddress = addressRepository.findById(addressId);

            optionalAddress.ifPresent(existingAddress -> {
                if (addressDTO != null) {
                    convertToEntity(addressDTO, existingAddress);
                    Address updatedAddress = addressRepository.save(existingAddress);
                    updatedAddressModels.add(convertToModel(updatedAddress));
                }
            });
        }
        return updatedAddressModels;
    }

    @Override
    @Transactional
    public boolean deleteById(Long id) {
        if (addressRepository.existsById(id)) {
            addressRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public boolean deleteAll(List<Long> ids) {
        List<Address> addressesToDelete = addressRepository.findAllById(ids);
        addressRepository.deleteAll(addressesToDelete);
        return true;
    }

    @Override
    @Transactional
    public List<AddressModel> search(String keywords) {
        SpecificationsBuilder<Address> specificationsBuilder = new SpecificationsBuilder<>();

        if (!StringUtils.isEmpty(keywords)) {
            specificationsBuilder.like("detail", keywords)
                    .or(builder -> {
                        builder.like("ward", keywords)
                                .like("district", keywords)
                                .like("city", keywords)
                                .like("country", keywords)
                                .like("zipCode", keywords)
                                .like("iframeAddress", keywords)
                                .like("addressType", keywords);
                    });
        }

        return addressRepository.findAll(specificationsBuilder.build()).stream()
                .map(this::convertToModel)
                .collect(Collectors.toList());
    }

    @Override
    public AddressModel convertToModel(Address address) {
        return new AddressModel(address.getId(), address.getUser().getId()
                , address.getDetail(), address.getWard(), address.getDistrict(), address.getCity(),
                address.getCountry(), address.getZipCode(), address.getIframeAddress(), address.getAddressType(),
                DateTimeUtils.formatLocalDateTime(address.getCreatedAt()), DateTimeUtils.formatLocalDateTime(address.getUpdatedAt()));
    }

    @Override
    public void convertToEntity(AddressDTO addressDTO, Address address) {
        Optional<User> user = userRepository.findById(addressDTO.getUserId());
        if (user.isPresent()) {
            address.setUser(user.get());
            address.setDetail(addressDTO.getDetail());
            address.setWard(addressDTO.getWard());
            address.setDistrict(addressDTO.getDistrict());
            address.setCity(addressDTO.getCity());
            address.setCountry(addressDTO.getCountry());
            address.setZipCode(addressDTO.getZipCode());
            address.setIframeAddress(addressDTO.getIframeAddress());
            address.setAddressType(addressDTO.getAddressType());
        }
    }
}
