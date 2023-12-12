package project.vegist.services;

import jakarta.transaction.Transactional;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import project.vegist.dtos.ArticleDTO;
import project.vegist.entities.ArticleTag;
import project.vegist.entities.Articles;
import project.vegist.entities.Tag;
import project.vegist.entities.User;
import project.vegist.exceptions.ResourceNotFoundException;
import project.vegist.models.ArticleModel;
import project.vegist.repositories.ArticleRepository;
import project.vegist.repositories.ArticleTagRepository;
import project.vegist.repositories.TagRepository;
import project.vegist.repositories.UserRepository;
import project.vegist.services.impls.CrudService;
import project.vegist.utils.DateTimeUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ArticleService implements CrudService<Articles, ArticleDTO, ArticleModel> {
    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final ArticleTagRepository articleTagRepository;

    @Autowired
    public ArticleService(ArticleRepository articleRepository, UserRepository userRepository, TagRepository tagRepository, ArticleTagRepository articleTagRepository) {
        this.articleRepository = articleRepository;
        this.userRepository = userRepository;
        this.tagRepository = tagRepository;
        this.articleTagRepository = articleTagRepository;
    }

    @Override
    public List<ArticleModel> findAll() {
        return articleRepository.findAll().stream()
                .map(this::convertToModel)
                .collect(Collectors.toList());
    }

    @Override
    public List<ArticleModel> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return articleRepository.findAll(pageable).getContent().stream()
                .map(this::convertToModel)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ArticleModel> findById(Long id) {
        return articleRepository.findById(id).map(this::convertToModel);
    }

    @Override
    @Transactional
    public Optional<ArticleModel> create(ArticleDTO articleDTO) {
        Articles newArticle = new Articles();
        convertToEntity(articleDTO, newArticle);

        Articles savedArticle = articleRepository.save(newArticle);

        List<Long> existingTagIds = savedArticle.getArticleTags().stream()
                .map(articleTag -> articleTag.getTag().getId())
                .toList();

        List<ArticleTag> articleTags = articleDTO.getTagIds().stream()
                .filter(tagId -> !existingTagIds.contains(tagId))
                .map(tagId -> {
                    Tag tag = tagRepository.findById(tagId)
                            .orElseThrow(() -> new RuntimeException("Tag not found"));
                    ArticleTag newArticleTag = new ArticleTag();
                    newArticleTag.setArticles(savedArticle);
                    newArticleTag.setTag(tag);
                    return newArticleTag;
                })
                .collect(Collectors.toList());

        List<ArticleTag> savedArticleTags = articleTagRepository.saveAll(articleTags);

        ArticleModel articleModel = convertToModel(savedArticle);

        return Optional.ofNullable(articleModel);
    }

    @Override
    @Transactional
    public List<ArticleModel> createAll(List<ArticleDTO> articleDTOS) {
        return articleDTOS.stream()
                .map(this::create)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Optional<ArticleModel> update(Long id, ArticleDTO articleDTO) {
        return articleRepository.findById(id).map(existingArticle -> {
            // Update fields from DTO
            existingArticle.setTitle(articleDTO.getTitle());
            existingArticle.setContent(articleDTO.getContent());
            existingArticle.setThumbnail(articleDTO.getThumbnail());
            existingArticle.setSeoTitle(articleDTO.getSeoTitle());
            existingArticle.setMetaKeys(articleDTO.getMetaKeys());
            existingArticle.setMetaDesc(articleDTO.getMetaDesc());
            existingArticle.setCreator(userRepository.findById(articleDTO.getCreatorId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", articleDTO.getCreatorId(), HttpStatus.CONFLICT)));

            Set<Long> existingTagIds = existingArticle.getArticleTags().stream()
                    .map(articleTag -> articleTag.getTag().getId())
                    .collect(Collectors.toSet());

            // Get user tag ids
            Set<Long> userTagIds = new HashSet<>(articleDTO.getTagIds());

            // Determine tags to remove and add
            Set<Long> tagsToRemove = existingTagIds.stream()
                    .filter(tagId -> !userTagIds.contains(tagId))
                    .collect(Collectors.toSet());

            Set<Long> tagsToAdd = userTagIds.stream()
                    .filter(tagId -> !existingTagIds.contains(tagId))
                    .collect(Collectors.toSet());

            articleTagRepository.deleteByArticlesIdAndTagsId(id, tagsToRemove);

            existingArticle.getArticleTags().removeIf(articleTag ->
                    tagsToRemove.contains(articleTag.getTag().getId()));

            existingTagIds.removeAll(tagsToRemove);
            existingTagIds.addAll(tagsToAdd);

            Articles updatedArticle = articleRepository.save(existingArticle);

            tagsToAdd.forEach(tagId -> {
                Tag tag = tagRepository.findById(tagId).orElseThrow(() -> new ResourceNotFoundException("Tag", tagId, HttpStatus.CONFLICT));
                ArticleTag articleTag = new ArticleTag();
                articleTag.setTag(tag);
                articleTag.setArticles(updatedArticle);
                articleTagRepository.save(articleTag);
            });

            Articles newArticle = new Articles();
            BeanUtils.copyProperties(updatedArticle, newArticle);
            List<ArticleTag> newArticleTags = existingTagIds.stream()
                    .map(tagId -> {
                        Tag tag = tagRepository.findById(tagId).orElseThrow(() -> new ResourceNotFoundException("Tag", tagId, HttpStatus.CONFLICT));
                        ArticleTag articleTag = new ArticleTag();
                        articleTag.setTag(tag);
                        articleTag.setArticles(newArticle);
                        return articleTag;
                    })
                    .collect(Collectors.toList());

            newArticle.setArticleTags(newArticleTags);
            return convertToModel(newArticle);
        });
    }

    @Override
    @Transactional
    public List<ArticleModel> updateAll(Map<Long, ArticleDTO> longArticleDTOMap) {
        return longArticleDTOMap.entrySet().stream()
                .map(entry -> update(entry.getKey(), entry.getValue()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean deleleById(Long id) {
        if (articleRepository.existsById(id)) {
            int affectedRows = articleTagRepository.deleteByArticlesId(id);
            if (affectedRows > 0) {
                articleRepository.deleteById(id);
                return true;
            }
        }
        return false;
    }

    @Override
    @Transactional
    public boolean deleteAll(List<Long> ids) {
        ids.forEach(this::deleteArticleTagsById);

        List<Articles> articlesToDelete = articleRepository.findAllById(ids);
        articleRepository.deleteAll(articlesToDelete);

        return true;
    }

    private void deleteArticleTagsById(Long id) {
        articleTagRepository.deleteByArticlesId(id);
    }

    @Override
    public List<ArticleModel> search(String keywords) {
        return null;
    }

    @Override
    public ArticleModel convertToModel(Articles articles) {
        return new ArticleModel(articles.getId(), articles.getTitle(), articles.getContent(),
                articles.getThumbnail(), articles.getSeoTitle(), articles.getMetaKeys(), articles.getMetaDesc(),
                articles.getCreator().getId(), articles.getArticleTags().stream().map(articleTag -> articleTag.getTag().getId()).collect(Collectors.toList()),
                DateTimeUtils.formatLocalDateTime(articles.getCreatedAt()), DateTimeUtils.formatLocalDateTime(articles.getUpdatedAt()));
    }

    @Override
    public void convertToEntity(ArticleDTO articleDTO, Articles articles) {
        Optional<User> user = userRepository.findById(articleDTO.getCreatorId());

        articles.setTitle(articleDTO.getTitle());
        articles.setContent(articleDTO.getContent());
        articles.setThumbnail(articleDTO.getThumbnail());
        articles.setSeoTitle(articleDTO.getSeoTitle());
        articles.setMetaKeys(articleDTO.getMetaKeys());
        articles.setMetaDesc(articleDTO.getMetaDesc());
        articles.setCreator(user.orElseThrow());

        List<ArticleTag> articleTags = articleDTO.getTagIds().stream()
                .map(tagId -> {
                    Tag tag = tagRepository.findById(tagId).orElseThrow();
                    ArticleTag articleTag = new ArticleTag();
                    articleTag.setTag(tag);
                    articleTag.setArticles(articles);
                    return articleTag;
                })
                .collect(Collectors.toList());

        articles.setArticleTags(articleTags);
    }
}
