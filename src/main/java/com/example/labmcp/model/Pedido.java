package com.example.labmcp.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * Pedido a domicilio de un cliente, almacenado en la tabla {@code pedido}.
 *
 * <p>Sus líneas ({@link DetallePedido}) se guardan y eliminan junto con él.</p>
 */
@Entity
@Table(name = "pedido")
public class Pedido {

    /** Identificador autoincremental generado por MySQL. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Cliente que hace el pedido. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    /** Estado actual; se guarda como texto para que la tabla sea legible. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoPedido estado;

    /** Dirección a la que se entrega este pedido. */
    @Column(name = "direccion_entrega", nullable = false)
    private String direccionEntrega;

    /** Momento en que se creó el pedido. */
    @Column(nullable = false)
    private LocalDateTime fecha;

    /** Total a pagar; se calcula al confirmar. */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    /** Líneas del pedido. */
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetallePedido> detalles = new ArrayList<>();

    /** Constructor vacío requerido por JPA. */
    protected Pedido() {}

    /**
     * Crea un pedido nuevo en estado {@link EstadoPedido#BORRADOR}.
     *
     * @param cliente          cliente que hace el pedido
     * @param direccionEntrega dirección de entrega
     * @param fecha            momento de creación
     */
    public Pedido(Cliente cliente, String direccionEntrega, LocalDateTime fecha) {
        this.cliente = cliente;
        this.direccionEntrega = direccionEntrega;
        this.fecha = fecha;
        this.estado = EstadoPedido.BORRADOR;
    }

    public Long getId() { return id; }

    public Cliente getCliente() { return cliente; }

    public EstadoPedido getEstado() { return estado; }
    public void setEstado(EstadoPedido estado) { this.estado = estado; }

    public String getDireccionEntrega() { return direccionEntrega; }
    public void setDireccionEntrega(String direccionEntrega) { this.direccionEntrega = direccionEntrega; }

    public LocalDateTime getFecha() { return fecha; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public List<DetallePedido> getDetalles() { return detalles; }
}
