package com.ecommerce.ecommerce_api.cart.repository;

import com.ecommerce.ecommerce_api.cart.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartRepository extends JpaRepository<Cart, UUID> {
    @Query("""
            select distinct c from Cart c
            left join fetch c.items i
            left join fetch i.product
            where c.user.id = :userId
            """)
    Optional<Cart> findByUserIdWithItems(@Param("userId") UUID userId);
}