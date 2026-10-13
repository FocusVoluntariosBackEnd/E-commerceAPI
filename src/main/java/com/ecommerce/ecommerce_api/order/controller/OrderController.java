package com.ecommerce.ecommerce_api.order.controller;

import com.ecommerce.ecommerce_api.order.dto.OrdersResponse;
import com.ecommerce.ecommerce_api.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.UUID;

@Tag(name = "Pedidos", description = "Histórico de pedidos do usuário autenticado")
@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    @Operation(summary = "Listar meus pedidos", description = "Retorna só os pedidos do usuário logado, do mais recente ao mais antigo.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de pedidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content)
    })
    @GetMapping
    public ResponseEntity<OrdersResponse> list(@Parameter(hidden = true) Principal principal) {
        OrdersResponse response = service.listByUser(UUID.fromString(principal.getName()));

        return ResponseEntity.ok(response);
    }
}