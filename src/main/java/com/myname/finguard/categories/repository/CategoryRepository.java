package com.myname.finguard.categories.repository;

import com.myname.finguard.categories.model.Category;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByUserId(Long userId);

    List<Category> findByUserIdOrUserIsNull(Long userId);

    Optional<Category> findByIdAndUserId(Long id, Long userId);
}
