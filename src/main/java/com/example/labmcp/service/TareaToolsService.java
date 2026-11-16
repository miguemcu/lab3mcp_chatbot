package com.example.labmcp.service;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.labmcp.model.Tarea;
import com.example.labmcp.repository.TareaRepository;

/**
 * Herramientas MCP para administrar tareas (CRUD + consultas).
 *
 * <p>Cada método {@link Tool} se publica en el servidor MCP: el nombre del método
 * es el nombre de la herramienta, {@code description} orienta al modelo sobre
 * cuándo usarla y los {@link ToolParam} generan el JSON Schema de los argumentos.</p>
 *
 * <p>Las validaciones se hacen aquí (lado servidor) y no se delegan al modelo.
 * Los errores se lanzan como {@link IllegalArgumentException}; Spring AI los
 * devuelve al cliente como resultado de error de la herramienta.</p>
 */
@Component
public class TareaToolsService {

    private static final String FORMATO_FECHA = "yyyy-MM-dd";

    private final TareaRepository repo;

    /**
     * @param repo repositorio de tareas inyectado por Spring
     */
    public TareaToolsService(TareaRepository repo) {
        this.repo = repo;
    }

    // ---------- READ ----------

    /**
     * Lista todas las tareas.
     *
     * @return todas las tareas almacenadas
     */
    @Tool(description = "Devuelve la lista completa de tareas almacenadas.")
    public List<Tarea> listarTareas() {
        return repo.findAll();
    }

    /**
     * Lista las tareas no completadas.
     *
     * @return tareas con {@code completado = false}
     */
    @Tool(description = "Devuelve únicamente las tareas pendientes (completado = false).")
    public List<Tarea> listarPendientes() {
        return repo.findByCompletado(false);
    }

    /**
     * Lista las tareas completadas.
     *
     * @return tareas con {@code completado = true}
     */
    @Tool(description = "Devuelve únicamente las tareas completadas (completado = true).")
    public List<Tarea> listarCompletadas() {
        return repo.findByCompletado(true);
    }

    /**
     * Cuenta las tareas almacenadas.
     *
     * @return número total de tareas
     */
    @Tool(description = "Devuelve el número total de tareas almacenadas.")
    public long contarTareas() {
        return repo.count();
    }

    /**
     * Busca tareas por fragmento del título (sin distinguir mayúsculas).
     *
     * @param fragmento texto a buscar; si viene vacío se devuelven todas
     * @return tareas coincidentes
     */
    @Tool(description = "Busca tareas cuyo título contiene el fragmento indicado (case-insensitive).")
    public List<Tarea> buscarTareas(
            @ToolParam(description = "Fragmento de texto a buscar en el título") String fragmento) {
        return repo.findByTituloContainingIgnoreCase(fragmento == null ? "" : fragmento.trim());
    }

    // ---------- CREATE ----------

    /**
     * Crea una tarea pendiente.
     *
     * @param titulo título obligatorio (se recortan espacios)
     * @param fecha  fecha asociada en formato {@code yyyy-MM-dd}, opcional
     * @return la tarea creada, con su id asignado
     * @throws IllegalArgumentException si el título es nulo o vacío, o la fecha no es válida
     */
    @Tool(description = "Crea una nueva tarea con el título y fecha indicados. Por defecto queda como pendiente.")
    @Transactional
    public Tarea crearTarea(
            @ToolParam(description = "Título descriptivo de la tarea") String titulo,
            @ToolParam(description = "Fecha asociada en formato yyyy-MM-dd, por ejemplo 2026-11-20 (opcional)", required = false) String fecha) {
        if (titulo == null || titulo.isBlank()) {
            throw new IllegalArgumentException("El título es obligatorio");
        }
        return repo.save(new Tarea(titulo.trim(), validarFecha(fecha), Boolean.FALSE));
    }

    // ---------- UPDATE ----------

    /**
     * Marca una tarea como completada.
     *
     * @param id identificador de la tarea
     * @return la tarea actualizada
     * @throws IllegalArgumentException si la tarea no existe
     */
    @Tool(description = "Marca como completada la tarea con el id indicado.")
    @Transactional
    public Tarea marcarCompletada(
            @ToolParam(description = "Identificador único de la tarea") Long id) {
        Tarea t = buscarPorId(id);
        t.setCompletado(Boolean.TRUE);
        return repo.save(t);
    }

    /**
     * Actualiza título y/o fecha. Los valores nulos o vacíos no se modifican.
     *
     * @param id     identificador de la tarea
     * @param titulo nuevo título, opcional
     * @param fecha  nueva fecha en formato {@code yyyy-MM-dd}, opcional
     * @return la tarea actualizada
     * @throws IllegalArgumentException si la tarea no existe o la fecha no es válida
     */
    @Tool(description = "Actualiza el título o la fecha de una tarea existente. Los parámetros nulos no se modifican.")
    @Transactional
    public Tarea actualizarTarea(
            @ToolParam(description = "Id de la tarea a actualizar") Long id,
            @ToolParam(description = "Nuevo título (opcional, null = no cambiar)", required = false) String titulo,
            @ToolParam(description = "Nueva fecha en formato yyyy-MM-dd (opcional, null = no cambiar)", required = false) String fecha) {
        String fechaValida = validarFecha(fecha);
        Tarea t = buscarPorId(id);
        if (titulo != null && !titulo.isBlank()) t.setTitulo(titulo.trim());
        if (fechaValida != null) t.setFecha(fechaValida);
        return repo.save(t);
    }

    // ---------- DELETE ----------

    /**
     * Elimina una tarea.
     *
     * @param id identificador de la tarea
     * @return mensaje de confirmación, o aviso si la tarea no existe
     */
    @Tool(description = "Elimina la tarea con el id indicado. Devuelve un mensaje de confirmación.")
    @Transactional
    public String eliminarTarea(
            @ToolParam(description = "Id de la tarea a eliminar") Long id) {
        if (!repo.existsById(id)) {
            return "La tarea " + id + " no existe.";
        }
        repo.deleteById(id);
        return "Tarea " + id + " eliminada correctamente.";
    }

    // ---------- Auxiliares ----------

    /** Obtiene la tarea o lanza {@link IllegalArgumentException} si no existe. */
    private Tarea buscarPorId(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No existe la tarea " + id));
    }

    /**
     * Valida que la fecha sea ISO ({@code yyyy-MM-dd}) y exista en el calendario.
     * Devuelve {@code null} si viene vacía, porque la fecha es opcional.
     */
    private static String validarFecha(String fecha) {
        if (fecha == null || fecha.isBlank()) {
            return null;
        }
        String valor = fecha.trim();
        try {
            LocalDate.parse(valor);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("La fecha '" + valor + "' no es válida; usa el formato "
                    + FORMATO_FECHA + ", por ejemplo 2026-11-20");
        }
        return valor;
    }
}
