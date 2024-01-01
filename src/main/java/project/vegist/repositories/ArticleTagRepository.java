package project.vegist.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import project.vegist.entities.ArticleTag;

import java.util.Set;

@Repository
public interface ArticleTagRepository extends JpaRepository<ArticleTag, Long>, JpaSpecificationExecutor<ArticleTag> {

    @Modifying
    @Query("DELETE FROM ArticleTag at WHERE at.articles.id = :articleId AND at.tag.id IN :tagIds")
    void deleteByArticlesIdAndTagsId(@Param("articleId") Long articleId, @Param("tagIds") Set<Long> tagIds);

    // Optional: Phương thức dùng để xóa tất cả ArticleTags của một Articles
    @Modifying
    @Query("DELETE FROM ArticleTag at WHERE at.articles.id = :articleId")
    int deleteByArticlesId(@Param("articleId") Long articleId);
}
