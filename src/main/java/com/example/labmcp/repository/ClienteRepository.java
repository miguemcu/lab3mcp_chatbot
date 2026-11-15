package com.example.labmcp.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.labmcp.model.Cliente;

/**
 * Repositorio Spring Data JPA para {@link Cliente}.
 */
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    /**
     * Busca un cliente por su teléfono, que es único.
     *
     * @param telefono teléfono exacto
     * @return el cliente, o vacío si no existe
     */
    Optional<Cliente> findByTelefono(String telefono);
}
