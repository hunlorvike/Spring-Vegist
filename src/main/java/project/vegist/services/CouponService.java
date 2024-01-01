package project.vegist.services;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.vegist.dtos.CouponDTO;
import project.vegist.entities.Coupon;
import project.vegist.models.CouponModel;
import project.vegist.repositories.CouponRepository;
import project.vegist.services.impls.CrudService;
import project.vegist.utils.DateTimeUtils;
import project.vegist.utils.SpecificationsBuilder;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CouponService implements CrudService<Coupon, CouponDTO, CouponModel> {
    private final CouponRepository couponRepository;

    @Autowired
    public CouponService(CouponRepository couponRepository) {
        this.couponRepository = couponRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CouponModel> findAll() {
        return couponRepository.findAll().stream()
                .map(this::convertToModel)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CouponModel> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return couponRepository.findAll(pageable).getContent().stream()
                .map(this::convertToModel)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CouponModel> findById(Long id) {
        return couponRepository.findById(id).map(this::convertToModel);
    }

    @Override
    @Transactional
    public Optional<CouponModel> create(CouponDTO couponDTO) {
        Objects.requireNonNull(couponDTO, "couponDTO must not be null");

        Coupon newCoupon = new Coupon();
        convertToEntity(couponDTO, newCoupon);
        return Optional.ofNullable(convertToModel(couponRepository.save(newCoupon)));
    }

    @Override
    @Transactional
    public List<CouponModel> createAll(List<CouponDTO> couponDTOS) {
        List<Coupon> newCoupons = couponDTOS.stream()
                .map(couponDTO -> {
                    Objects.requireNonNull(couponDTO, "couponDTO must not be null");

                    Coupon newCoupon = new Coupon();
                    convertToEntity(couponDTO, newCoupon);
                    return newCoupon;
                })
                .collect(Collectors.toList());

        return couponRepository.saveAll(newCoupons)
                .stream().map(this::convertToModel)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Optional<CouponModel> update(Long id, CouponDTO couponDTO) {
        return couponRepository.findById(id)
                .map(existingCoupon -> {
                    Objects.requireNonNull(couponDTO, "couponDTO must not be null");

                    convertToEntity(couponDTO, existingCoupon);
                    return convertToModel(couponRepository.save(existingCoupon));
                });
    }

    @Override
    @Transactional
    public List<CouponModel> updateAll(Map<Long, CouponDTO> longCouponDTOMap) {
        return longCouponDTOMap.entrySet().stream()
                .map(entry -> {
                    Long couponId = entry.getKey();
                    CouponDTO couponDTO = entry.getValue();

                    if (couponDTO != null) {
                        return couponRepository.findById(couponId)
                                .map(existingCoupon -> {
                                    convertToEntity(couponDTO, existingCoupon);
                                    Coupon updatedCoupon = couponRepository.save(existingCoupon);
                                    return convertToModel(updatedCoupon);
                                })
                                .orElse(null);
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean deleteById(Long id) {
        return couponRepository.existsById(id) && performDelete(id);
    }

    @Override
    @Transactional
    public boolean deleteAll(List<Long> ids) {
        List<Coupon> couponsToDelete = couponRepository.findAllById(ids);
        if (!couponsToDelete.isEmpty()) {
            couponRepository.deleteAll(couponsToDelete);
            return true;
        }
        return false;
    }

    @Override
    public List<CouponModel> search(String keywords) {
        SpecificationsBuilder<Coupon> specificationsBuilder = new SpecificationsBuilder<>();

        if (!StringUtils.isEmpty(keywords)) {
            specificationsBuilder
                    .like("value", keywords) // Add more fields if needed
                    .or(builder -> {
                        builder.like("percent", keywords);
                        builder.like("startDate", keywords);
                        builder.like("endDate", keywords);
                    });
        }

        Specification<Coupon> spec = specificationsBuilder.build();

        return couponRepository.findAll(spec).stream()
                .map(this::convertToModel)
                .collect(Collectors.toList());
    }

    @Override
    public CouponModel convertToModel(Coupon coupon) {
        Objects.requireNonNull(coupon, "coupon must not be null");

        return new CouponModel(coupon.getId(), coupon.getValue(), coupon.getPercent(),
                DateTimeUtils.formatLocalDateTime(coupon.getStartDate()), DateTimeUtils.formatLocalDateTime(coupon.getEndDate()));
    }

    @Override
    public void convertToEntity(CouponDTO couponDTO, Coupon coupon) {
        Objects.requireNonNull(couponDTO, "couponDTO must not be null");
        Objects.requireNonNull(coupon, "coupon must not be null");

        coupon.setValue(couponDTO.getValue());
        coupon.setPercent(couponDTO.getPercent());
        coupon.setStartDate(couponDTO.getStartDate());
        coupon.setEndDate(couponDTO.getEndDate());
    }

    private boolean performDelete(Long id) {
        couponRepository.deleteById(id);
        return true;
    }
}
