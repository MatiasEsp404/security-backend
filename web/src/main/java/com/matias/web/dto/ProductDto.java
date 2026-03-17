package com.matias.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Información de un producto")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {
    
    @Schema(description = "ID del producto", example = "1")
    private Long id;
    
    @Schema(description = "Nombre del producto", example = "Laptop")
    private String name;
    
    @Schema(description = "Descripción del producto", example = "Laptop de alta gama")
    private String description;
    
    @Schema(description = "Precio del producto", example = "999.99")
    private Double price;
}
