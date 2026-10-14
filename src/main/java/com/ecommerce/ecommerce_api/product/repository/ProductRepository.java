package com.ecommerce.ecommerce_api.product.repository;

import com.ecommerce.ecommerce_api.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {
    Optional<Product> findByIdAndActiveTrue(UUID id);

    Page<Product> findAllByActiveTrue(Pageable pageable);

    Page<Product> findByNameContainingIgnoreCaseAndActiveTrue(String name, Pageable pageable);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
    UPDATE Product p
    SET p.stock = p.stock - :quantity,
        p.version = p.version + 1
    WHERE p.id = :id AND p.stock >= :quantity
    """)
    int decreaseStock(UUID id, int quantity);
}
