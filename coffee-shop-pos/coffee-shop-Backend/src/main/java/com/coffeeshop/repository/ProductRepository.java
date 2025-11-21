package com.coffeeshop.repository;

import com.coffeeshop.model.Category;
import com.coffeeshop.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategory(Category category);
    List<Product> findByAvailableTrue();
    List<Product> findByCategoryAndAvailableTrue(Category category);
}