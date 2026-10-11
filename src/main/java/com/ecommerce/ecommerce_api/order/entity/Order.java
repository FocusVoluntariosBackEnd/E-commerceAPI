package com.ecommerce.ecommerce_api.order.entity;

import com.ecommerce.ecommerce_api.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Getter
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PENDING;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Order() {}

    public Order(User user) {
        if (user == null)
            throw new IllegalArgumentException("User can't be null");

        this.user = user;
    }

    private void addItem(OrderItem item) {
        if (item == null)
            throw new IllegalArgumentException("Item can't be null");

        this.items.add(item);

        item.setOrder(this);
    }

    public void calculateTotalAmount() {
        BigDecimal totalAmount = items.stream()
                .map(OrderItem::calculateTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        this.totalAmount = totalAmount;
    }
}