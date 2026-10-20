package com.ecommerce.ecommerce_api.cart.entity;

import com.ecommerce.ecommerce_api.exception.BusinessRuleException;
import com.ecommerce.ecommerce_api.exception.ResourceNotFoundException;
import com.ecommerce.ecommerce_api.product.entity.Product;
import com.ecommerce.ecommerce_api.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "carts")
@Getter
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> items = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    protected Cart() {
    }

    public Cart(User user) {
        this.user = user;
    }

    public void addItem(Product product, int quantity) {
        findItemByProduct(product).ifPresentOrElse(
                existingItem -> {
                    int newQuantity = existingItem.getQuantity() + quantity;

                    if (!product.isStockAvailable(newQuantity))
                        throw new BusinessRuleException("Insufficient stock to add to cart.");

                    existingItem.updateQuantity(newQuantity);
                },
                () -> {
                    if (!product.isStockAvailable(quantity))
                        throw new BusinessRuleException("Insufficient stock to add to cart.");

                    items.add(new CartItem(this, product, quantity));
                }
        );
    }

    public void updateQuantityItem(Product product, int quantity) {
        findItemByProduct(product).ifPresentOrElse(
                item -> {
                    if (!product.isStockAvailable(quantity))
                        throw new BusinessRuleException("Insufficient stock to add to cart.");

                    item.updateQuantity(quantity);
                },
                () -> { throw new ResourceNotFoundException("Cart item not found."); }
        );
    }

    public void removeItem(UUID productId) {
        CartItem item = items.stream()
                .filter(i -> i.getProduct().getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found."));

        items.remove(item);
    }

    private Optional<CartItem> findItemByProduct(Product product) {
        return items.stream()
                .filter(i -> i.getProduct().equals(product))
                .findFirst();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public void clear() {
        items.clear();
    }

    public List<CartItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public List<CartItem> getItemsSortedByProductId() {
        return items.stream()
                .sorted(Comparator.comparing(i -> i.getProduct().getId()))
                .toList();
    }

    public BigDecimal getTotal() {
        return items.stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}