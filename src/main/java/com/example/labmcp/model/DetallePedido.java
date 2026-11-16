package com.example.labmcp.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Línea de un pedido (producto, cantidad y precio), almacenada en la tabla {@code detalle_pedido}.
 */
@Entity
@Table(name = "detalle_pedido")
public class DetallePedido {

    /** Identificador autoincremental generado por MySQL. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Pedido al que pertenece la línea. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    /** Producto pedido. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    /** Unidades pedidas; siempre mayor que cero. */
    @Column(nullable = false)
    private int cantidad;

    /** Precio al momento de agregar el producto, para que un cambio de precio posterior no altere el pedido. */
    @Column(name = "precio_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioUnitario;

    /** Constructor vacío requerido por JPA. */
    protected DetallePedido() {}

    /**
     * Crea una línea de pedido.
     *
     * @param pedido         pedido al que pertenece
     * @param producto       producto pedido
     * @param cantidad       unidades pedidas
     * @param precioUnitario precio del producto al agregarlo
     */
    public DetallePedido(Pedido pedido, Producto producto, int cantidad, BigDecimal precioUnitario) {
        this.pedido = pedido;
        this.producto = producto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
    }

    public Long getId() { return id; }

    public Pedido getPedido() { return pedido; }

    public Producto getProducto() { return producto; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public BigDecimal getPrecioUnitario() { return precioUnitario; }
}
