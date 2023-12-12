package project.vegist.controllers;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.vegist.dtos.ArticleDTO;
import project.vegist.exceptions.ResourceNotFoundException;
import project.vegist.models.ArticleModel;
import project.vegist.repositories.UserRepository;
import project.vegist.responses.BaseResponse;
import project.vegist.responses.ErrorResponse;
import project.vegist.responses.SuccessResponse;
import project.vegist.services.ArticleService;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/public")
public class ArticleController {
    private final UserRepository userRepository;
    private final ArticleService articleService;

    @Autowired
    public ArticleController(UserRepository userRepository, ArticleService articleService) {
        this.userRepository = userRepository;
        this.articleService = articleService;
    }

    @GetMapping("/articles")
    public ResponseEntity<BaseResponse<List<ArticleModel>>> getAllArticles(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        try {
            List<ArticleModel> articles = articleService.findAll(page, size);
            return new ResponseEntity<>(new SuccessResponse<>(articles), HttpStatus.OK);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(e.getStatus()).body(new ErrorResponse<>(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse<>(e.getMessage()));
        }
    }

    @GetMapping("/articles/{id}")
    public ResponseEntity<BaseResponse<ArticleModel>> getArticleById(@PathVariable Long id) {
        try {
            Optional<ArticleModel> articles = articleService.findById(id);
            return articles.map(value -> ResponseEntity.ok((BaseResponse<ArticleModel>) new SuccessResponse<>(value, null)))
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(new BaseResponse<>("failed", "Article not found", null)));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(e.getStatus()).body(new ErrorResponse<>(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse<>(e.getMessage()));
        }
    }

    @PostMapping("/articles")
    public ResponseEntity<BaseResponse<ArticleModel>> createArticle(@Valid @RequestBody ArticleDTO articleDTO) {
        try {
            Optional<ArticleModel> createdArticle = articleService.create(articleDTO);
            return createdArticle.map(value -> ResponseEntity.ok(new BaseResponse<>("success", "Article created", value)))
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(new ErrorResponse<>(Collections.singletonList("Failed to create article"))));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse<>(e.getMessage()));
        }
    }

    @PutMapping("/articles/{id}")
    public ResponseEntity<BaseResponse<ArticleModel>> updateArticle(@PathVariable Long id, @Valid @RequestBody ArticleDTO articleDTO) {
        try {
            Optional<ArticleModel> updatedArticle = articleService.update(id, articleDTO);
            return updatedArticle.map(value -> ResponseEntity.ok(new BaseResponse<>("success", "Article updated", value)))
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(new ErrorResponse<>(Collections.singletonList("Article not found"))));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse<>(e.getMessage()));
        }
    }

    @DeleteMapping("/articles/{id}")
    public ResponseEntity<BaseResponse<String>> deleteArticle(@PathVariable Long id) {
        try {
            boolean isDeleted = articleService.deleleById(id);
            return isDeleted
                    ? ResponseEntity.ok(new SuccessResponse<>("Article deleted"))
                    : ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse<>("Article not found"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse<>(e.getMessage()));
        }
    }
}
