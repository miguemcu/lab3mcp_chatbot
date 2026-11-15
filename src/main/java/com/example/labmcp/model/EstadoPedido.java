package com.example.labmcp.model;

/**
 * Estados por los que pasa un pedido del supermercado.
 */
public enum EstadoPedido {

    /** El cliente aún puede agregar productos; el stock no se ha descontado. */
    BORRADOR,

    /** El pedido se aceptó y el stock ya se descontó. */
    CONFIRMADO,

    /** El pedido se anuló; si estaba confirmado, el stock se repuso. */
    CANCELADO
}
