package project.vegist.services;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.vegist.dtos.ReviewDTO;
import project.vegist.entities.Product;
import project.vegist.entities.Review;
import project.vegist.entities.User;
import project.vegist.exceptions.ResourceNotFoundException;
import project.vegist.models.ReviewModel;
import project.vegist.repositories.ProductRepository;
import project.vegist.repositories.ReviewRepository;
import project.vegist.repositories.UserRepository;
import project.vegist.services.impls.CrudService;
import project.vegist.utils.DateTimeUtils;
import project.vegist.utils.SpecificationsBuilder;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ReviewService implements CrudService<Review, ReviewDTO, ReviewModel> {
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ReviewRepository reviewRepository;

    @Autowired
    public ReviewService(UserRepository userRepository, ProductRepository productRepository, ReviewRepository reviewRepository) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.reviewRepository = reviewRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewModel> findAll() {
        return reviewRepository.findAll().stream()
                .map(this::convertToModel)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewModel> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return reviewRepository.findAll(pageable).getContent().stream()
                .map(this::convertToModel)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ReviewModel> findById(Long id) {
        return reviewRepository.findById(id).map(this::convertToModel);
    }

    @Override
    @Transactional
    public Optional<ReviewModel> create(ReviewDTO reviewDTO) throws IOException {
        Review newReview = new Review();
        convertToEntity(reviewDTO, newReview);
        return Optional.ofNullable(convertToModel(reviewRepository.save(newReview)));
    }

    @Override
    @Transactional
    public List<ReviewModel> createAll(List<ReviewDTO> reviewDTOS) throws IOException {
        List<Review> newReviews = reviewDTOS.stream()
                .map(reviewDTO -> {
                    Review newReview = new Review();
                    convertToEntity(reviewDTO, newReview);
                    return newReview;
                })
                .collect(Collectors.toList());

        List<Review> savedReviews = reviewRepository.saveAll(newReviews);
        return savedReviews.stream()
                .map(this::convertToModel)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Optional<ReviewModel> update(Long id, ReviewDTO reviewDTO) {
        return reviewRepository.findById(id)
                .map(existingReview -> {
                    convertToEntity(reviewDTO, existingReview);
                    Review updatedReview = reviewRepository.save(existingReview);
                    return convertToModel(updatedReview);
                });
    }

    @Override
    @Transactional
    public List<ReviewModel> updateAll(Map<Long, ReviewDTO> longReviewDTOMap) {
        List<Review> reviewsToUpdate = longReviewDTOMap.entrySet().stream()
                .map(entry -> {
                    Long reviewId = entry.getKey();
                    ReviewDTO reviewDTO = entry.getValue();
                    Review existingReview = reviewRepository.findById(reviewId)
                            .orElseThrow(() -> new ResourceNotFoundException("Review", reviewId, HttpStatus.NOT_FOUND));

                    convertToEntity(reviewDTO, existingReview);
                    return existingReview;
                })
                .collect(Collectors.toList());

        List<Review> updatedReviews = reviewRepository.saveAll(reviewsToUpdate);
        return updatedReviews.stream()
                .map(this::convertToModel)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean deleteById(Long id) {
        if (reviewRepository.existsById(id)) {
            reviewRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public boolean deleteAll(List<Long> ids) {
        List<Review> reviewsToDelete = reviewRepository.findAllById(ids);
        reviewRepository.deleteAll(reviewsToDelete);
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewModel> search(String keywords) {
        SpecificationsBuilder<Review> specificationsBuilder = new SpecificationsBuilder<>();

        if (!StringUtils.isEmpty(keywords)) {
            specificationsBuilder
                    .or(builder -> {
                        builder.like("user.username", keywords); // assuming you have a 'username' field in the 'User' entity
                        builder.like("product.productName", keywords); // assuming you have a 'productName' field in the 'Product' entity
                        // Add additional search conditions if needed
                        // builder.like("anotherField", keywords);
                    });
        }

        Specification<Review> spec = specificationsBuilder.build();

        return reviewRepository.findAll(spec).stream()
                .map(this::convertToModel)
                .collect(Collectors.toList());
    }


    @Override
    public ReviewModel convertToModel(Review review) {
        User user = userRepository.findById(review.getUser().getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", review.getUser().getId(), HttpStatus.NOT_FOUND));

        Product product = productRepository.findById(review.getProduct().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", review.getProduct().getId(), HttpStatus.NOT_FOUND));

        return new ReviewModel(
                review.getId(),
                user.getId(),
                user,
                product.getId(),
                product,
                review.getRating(),
                DateTimeUtils.formatLocalDateTime(review.getCreatedAt()),
                DateTimeUtils.formatLocalDateTime(review.getUpdatedAt())
        );
    }

    @Override
    public void convertToEntity(ReviewDTO reviewDTO, Review review) {
        User user = userRepository.findById(reviewDTO.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", reviewDTO.getUserId(), HttpStatus.NOT_FOUND));

        Product product = productRepository.findById(reviewDTO.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", reviewDTO.getProductId(), HttpStatus.NOT_FOUND));

        review.setUser(user);
        review.setProduct(product);
        review.setRating(reviewDTO.getRating());
    }
}
