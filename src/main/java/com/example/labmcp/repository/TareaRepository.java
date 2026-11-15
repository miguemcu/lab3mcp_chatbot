package com.example.labmcp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.labmcp.model.Tarea;

/**
 * Repositorio Spring Data JPA para {@link Tarea}.
 *
 * <p>Hereda el CRUD básico de {@link JpaRepository}; las consultas adicionales
 * se derivan del nombre del método (no requieren SQL explícito).</p>
 */
public interface TareaRepository extends JpaRepository<Tarea, Long> {

    /**
     * Filtra tareas por estado.
     *
     * @param completado {@code true} para completadas, {@code false} para pendientes
     * @return tareas con ese estado
     */
    List<Tarea> findByCompletado(Boolean completado);

    /**
     * Busca tareas cuyo título contiene el fragmento, sin distinguir mayúsculas.
     *
     * @param fragmento texto a buscar
     * @return tareas coincidentes
     */
    List<Tarea> findByTituloContainingIgnoreCase(String fragmento);
}
