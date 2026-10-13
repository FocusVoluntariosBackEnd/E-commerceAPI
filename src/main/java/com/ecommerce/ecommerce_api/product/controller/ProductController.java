package com.ecommerce.ecommerce_api.product.controller;

import com.ecommerce.ecommerce_api.product.dto.CreateProductRequest;
import com.ecommerce.ecommerce_api.product.dto.ProductResponse;
import com.ecommerce.ecommerce_api.product.dto.UpdateProductRequest;
import com.ecommerce.ecommerce_api.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Produtos", description = "Catálogo de produtos")
@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @Operation(summary = "Criar produto")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Produto criado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada", content = @Content)
    })
    @PostMapping
    public ResponseEntity<ProductResponse> create(@RequestBody @Valid CreateProductRequest request) {
        ProductResponse response = service.create(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Listar produtos",
            description = "Lista produtos ativos com paginação (page começa em 1) e busca opcional por nome.")
    @SecurityRequirements
    @ApiResponse(responseCode = "200", description = "Página de produtos")
    @GetMapping
    public ResponseEntity<Page<ProductResponse>> list(
            @Parameter(description = "Número da página (começa em 1)", example = "1")
            @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Itens por página", example = "10")
            @RequestParam(defaultValue = "10") int limit,
            @Parameter(description = "Busca por nome", example = "notebook")
            @RequestParam(required = false) String search
    ) {
        Page<ProductResponse> response = service.list(page, limit, search);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Detalhar produto", description = "Retorna os detalhes de um produto ativo. Rota pública.")
    @SecurityRequirements
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Produto encontrado"),
            @ApiResponse(responseCode = "400", description = "ID inválido", content = @Content),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado ou inativo", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getById(@PathVariable UUID id) {
        ProductResponse response = service.getById(id);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Atualizar produto",
            description = "Exige a version atual do produto (controle de concorrência otimista).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Produto atualizado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Produto (ou produto inativo) ou categoria não encontrado", content = @Content),
            @ApiResponse(responseCode = "409", description = "Produto alterado por outro usuário (version desatualizada)", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> update(@PathVariable UUID id,
                                                  @RequestBody @Valid UpdateProductRequest request) {
        ProductResponse response = service.update(id, request);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Remover produto",
            description = "Exclusão lógica: o produto fica inativo e o histórico de pedidos não é afetado.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Produto removido"),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}