package com.ecommerce.ecommerce_api.cart.controller;

import com.ecommerce.ecommerce_api.cart.dto.AddCartItemRequest;
import com.ecommerce.ecommerce_api.cart.dto.CartResponse;
import com.ecommerce.ecommerce_api.cart.dto.UpdateCartItemQuantityRequest;
import com.ecommerce.ecommerce_api.cart.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Carrinho", description = "Carrinho do usuário autenticado")
@RestController
@RequestMapping("/api/cart")
public class CartController {
    private final CartService service;

    public CartController(CartService service) {
        this.service = service;
    }

    @Operation(summary = "Adicionar item",
            description = "Se o produto já estiver no carrinho, soma a quantidade, respeitando o estoque.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Item adicionado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado", content = @Content),
            @ApiResponse(responseCode = "409", description = "Estoque insuficiente", content = @Content)
    })
    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItem(@Parameter(hidden = true) @AuthenticationPrincipal UUID userId,
                                                @Valid @RequestBody AddCartItemRequest request) {
        CartResponse response = service.addItem(userId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Consultar carrinho", description = "Retorna os itens, subtotais e o total.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Carrinho do usuário"),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content)
    })
    @GetMapping
    public ResponseEntity<CartResponse> getCart(@Parameter(hidden = true) @AuthenticationPrincipal UUID userId) {
        CartResponse response = service.getCart(userId);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Alterar quantidade", description = "Define a nova quantidade do item. Não pode ultrapassar o estoque.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Quantidade atualizada"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Carrinho, produto ou item não encontrado", content = @Content),
            @ApiResponse(responseCode = "409", description = "Estoque insuficiente", content = @Content)
    })
    @PutMapping("/items/{productId}")
    public ResponseEntity<CartResponse> updateQuantityCartItem(@Parameter(hidden = true) @AuthenticationPrincipal UUID userId,
                                                               @Parameter(description = "ID do produto") @PathVariable UUID productId,
                                                               @Valid @RequestBody UpdateCartItemQuantityRequest request) {
        CartResponse response = service.updateQuantityCartItem(userId, productId, request);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Remover item")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Item removido"),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Carrinho ou item não encontrado", content = @Content)
    })
    @DeleteMapping("/items/{productId}")
    public ResponseEntity<Void> removeItem(@Parameter(hidden = true) @AuthenticationPrincipal UUID userId,
                                           @Parameter(description = "ID do produto") @PathVariable UUID productId) {
        service.removeItem(userId, productId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}