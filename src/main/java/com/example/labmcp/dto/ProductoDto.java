package com.example.labmcp.dto;

import java.math.BigDecimal;

import com.example.labmcp.model.Producto;

/**
 * Producto tal como lo ve el cliente MCP.
 */
public record ProductoDto(Long id, String nombre, String categoria, BigDecimal precio, int stock) {

    /** Convierte la entidad en DTO. */
    public static ProductoDto de(Producto producto) {
        return new ProductoDto(producto.getId(), producto.getNombre(), producto.getCategoria(),
                producto.getPrecio(), producto.getStock());
    }
}
