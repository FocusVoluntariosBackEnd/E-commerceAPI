package com.ecommerce.ecommerce_api.cart.entity;

import com.ecommerce.ecommerce_api.product.entity.Product;
import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "cart_items")
@Getter
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "unit_price", nullable = false)
    private BigDecimal unitPrice;

    protected CartItem() {}

    public CartItem(Cart cart, Product product, int quantity) {
        if (cart == null)
            throw new IllegalArgumentException("Cart can't be null");

        this.cart = cart;

        if (product == null)
            throw new IllegalArgumentException("Product can't be null");

        this.product = product;

        if (quantity <= 0)
            throw new IllegalArgumentException("Quantity can't be negative or 0");

        this.quantity = quantity;

        this.unitPrice = product.getPrice();
    }

    public void setCart(Cart cart) {
        this.cart = cart;
    }

    public BigDecimal getSubtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}