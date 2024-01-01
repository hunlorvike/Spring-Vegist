package project.vegist.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import project.vegist.entities.Articles;

@Repository
public interface ArticleRepository extends JpaRepository<Articles, Long>, JpaSpecificationExecutor<Articles> {
}
