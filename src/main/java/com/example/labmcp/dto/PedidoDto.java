package com.example.labmcp.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.example.labmcp.model.EstadoPedido;
import com.example.labmcp.model.Pedido;

/**
 * Pedido con sus líneas y el total, tal como lo ve el cliente MCP.
 */
public record PedidoDto(Long id, Long clienteId, String cliente, EstadoPedido estado, String direccionEntrega,
        LocalDateTime fecha, List<LineaPedidoDto> lineas, BigDecimal total) {

    /** Convierte la entidad en DTO; el total es la suma de los subtotales de las líneas. */
    public static PedidoDto de(Pedido pedido) {
        List<LineaPedidoDto> lineas = pedido.getDetalles().stream().map(LineaPedidoDto::de).toList();
        BigDecimal total = lineas.stream().map(LineaPedidoDto::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new PedidoDto(pedido.getId(), pedido.getCliente().getId(), pedido.getCliente().getNombre(),
                pedido.getEstado(), pedido.getDireccionEntrega(), pedido.getFecha(), lineas, total);
    }
}
