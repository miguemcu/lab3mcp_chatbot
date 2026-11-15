package com.example.labmcp.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

/**
 * Producto del catálogo del supermercado, almacenado en la tabla {@code producto}.
 */
@Entity
@Table(name = "producto")
public class Producto {

    /** Identificador autoincremental generado por MySQL. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nombre comercial; único en el catálogo. */
    @Column(nullable = false, unique = true, length = 100)
    private String nombre;

    /** Categoría a la que pertenece (por ejemplo, Lácteos). */
    @Column(nullable = false, length = 50)
    private String categoria;

    /** Precio unitario en pesos. */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    /** Unidades disponibles. */
    @Column(nullable = false)
    private int stock;

    /** Versión para bloqueo optimista; evita descontar el mismo stock dos veces en paralelo. */
    @Version
    private Long version;

    /** Constructor vacío requerido por JPA. */
    protected Producto() {}

    /**
     * Crea un producto nuevo (sin id; lo asigna la base de datos).
     *
     * @param nombre    nombre comercial
     * @param categoria categoría del producto
     * @param precio    precio unitario
     * @param stock     unidades disponibles
     */
    public Producto(String nombre, String categoria, BigDecimal precio, int stock) {
        this.nombre = nombre;
        this.categoria = categoria;
        this.precio = precio;
        this.stock = stock;
    }

    public Long getId() { return id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public Long getVersion() { return version; }
}
