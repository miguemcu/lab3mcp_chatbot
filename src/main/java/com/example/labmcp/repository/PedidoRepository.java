package com.example.labmcp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.labmcp.model.Pedido;

/**
 * Repositorio Spring Data JPA para {@link Pedido}.
 */
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
}
