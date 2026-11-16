package com.example.labmcp.dto;

import java.math.BigDecimal;

import com.example.labmcp.model.DetallePedido;

/**
 * Línea de un pedido con su subtotal.
 */
public record LineaPedidoDto(Long productoId, String producto, int cantidad, BigDecimal precioUnitario,
        BigDecimal subtotal) {

    /** Convierte la entidad en DTO y calcula el subtotal. */
    public static LineaPedidoDto de(DetallePedido detalle) {
        BigDecimal subtotal = detalle.getPrecioUnitario().multiply(BigDecimal.valueOf(detalle.getCantidad()));
        return new LineaPedidoDto(detalle.getProducto().getId(), detalle.getProducto().getNombre(),
                detalle.getCantidad(), detalle.getPrecioUnitario(), subtotal);
    }
}
