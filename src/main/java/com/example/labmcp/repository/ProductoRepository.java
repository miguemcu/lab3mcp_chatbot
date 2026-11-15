package com.example.labmcp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.labmcp.model.Producto;

/**
 * Repositorio Spring Data JPA para {@link Producto}.
 */
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    /**
     * Busca productos cuyo nombre o categoría contienen el texto, sin distinguir mayúsculas.
     *
     * <p>Se pasa el mismo texto en los dos parámetros: Spring Data pide uno por condición.</p>
     *
     * @param nombre    fragmento a buscar en el nombre
     * @param categoria fragmento a buscar en la categoría
     * @return productos coincidentes
     */
    List<Producto> findByNombreContainingIgnoreCaseOrCategoriaContainingIgnoreCase(String nombre, String categoria);
}
