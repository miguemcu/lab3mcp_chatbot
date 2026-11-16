package com.example.labmcp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.labmcp.model.DetallePedido;

/**
 * Repositorio Spring Data JPA para {@link DetallePedido}.
 */
public interface DetallePedidoRepository extends JpaRepository<DetallePedido, Long> {
}
