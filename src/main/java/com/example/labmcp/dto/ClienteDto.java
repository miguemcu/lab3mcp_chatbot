package com.example.labmcp.dto;

import com.example.labmcp.model.Cliente;

/**
 * Cliente tal como lo ve el cliente MCP.
 */
public record ClienteDto(Long id, String nombre, String telefono, String direccion) {

    /** Convierte la entidad en DTO. */
    public static ClienteDto de(Cliente cliente) {
        return new ClienteDto(cliente.getId(), cliente.getNombre(), cliente.getTelefono(), cliente.getDireccion());
    }
}
