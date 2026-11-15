package com.example.labmcp.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Cliente del supermercado, almacenado en la tabla {@code cliente}.
 *
 * <p>El teléfono es único: es el dato con el que el chatbot identifica al cliente.</p>
 */
@Entity
@Table(name = "cliente")
public class Cliente {

    /** Identificador autoincremental generado por MySQL. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nombre completo. */
    @Column(nullable = false, length = 100)
    private String nombre;

    /** Teléfono de contacto; no puede repetirse entre clientes. */
    @Column(nullable = false, unique = true, length = 20)
    private String telefono;

    /** Dirección habitual de entrega. */
    @Column(nullable = false)
    private String direccion;

    /** Constructor vacío requerido por JPA. */
    protected Cliente() {}

    /**
     * Crea un cliente nuevo (sin id; lo asigna la base de datos).
     *
     * @param nombre    nombre completo
     * @param telefono  teléfono de contacto, único
     * @param direccion dirección habitual de entrega
     */
    public Cliente(String nombre, String telefono, String direccion) {
        this.nombre = nombre;
        this.telefono = telefono;
        this.direccion = direccion;
    }

    public Long getId() { return id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
}
