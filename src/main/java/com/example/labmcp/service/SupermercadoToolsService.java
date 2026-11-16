package com.example.labmcp.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.labmcp.dto.ClienteDto;
import com.example.labmcp.dto.PedidoDto;
import com.example.labmcp.dto.ProductoDto;
import com.example.labmcp.model.Cliente;
import com.example.labmcp.model.DetallePedido;
import com.example.labmcp.model.EstadoPedido;
import com.example.labmcp.model.Pedido;
import com.example.labmcp.model.Producto;
import com.example.labmcp.repository.ClienteRepository;
import com.example.labmcp.repository.PedidoRepository;
import com.example.labmcp.repository.ProductoRepository;

/**
 * Herramientas MCP del supermercado a domicilio: catálogo, clientes y pedidos.
 *
 * <p>Un pedido nace en {@code BORRADOR}, se le agregan productos y luego se confirma
 * (descuenta stock) o se cancela (repone stock si estaba confirmado). Los errores de
 * negocio se lanzan como {@link IllegalArgumentException} y llegan al cliente como
 * {@code isError: true}.</p>
 */
@Component
public class SupermercadoToolsService {

    private static final String NO_EXISTE_PEDIDO = "No existe el pedido %d";
    private static final String NO_EXISTE_PRODUCTO = "No existe el producto %d";
    private static final String NO_EXISTE_CLIENTE = "No existe el cliente %d";
    private static final String STOCK_INSUFICIENTE = "Stock insuficiente de %s: hay %d y se piden %d";

    private final ProductoRepository productoRepo;
    private final ClienteRepository clienteRepo;
    private final PedidoRepository pedidoRepo;

    /**
     * @param productoRepo repositorio de productos
     * @param clienteRepo  repositorio de clientes
     * @param pedidoRepo   repositorio de pedidos
     */
    public SupermercadoToolsService(ProductoRepository productoRepo, ClienteRepository clienteRepo,
            PedidoRepository pedidoRepo) {
        this.productoRepo = productoRepo;
        this.clienteRepo = clienteRepo;
        this.pedidoRepo = pedidoRepo;
    }

    /**
     * Busca productos por nombre o categoría.
     *
     * @param texto fragmento a buscar; vacío devuelve todo el catálogo
     * @return productos coincidentes
     */
    @Tool(description = "Busca productos del supermercado cuyo nombre o categoría contienen el texto "
            + "(sin distinguir mayúsculas). Devuelve id, nombre, categoría, precio y stock.")
    public List<ProductoDto> buscarProductos(
            @ToolParam(description = "Texto a buscar, por ejemplo 'leche' o 'lácteos'") String texto) {
        String filtro = texto == null ? "" : texto.trim();
        return productoRepo.findByNombreContainingIgnoreCaseOrCategoriaContainingIgnoreCase(filtro, filtro)
                .stream().map(ProductoDto::de).toList();
    }

    /**
     * Registra un cliente o devuelve el existente si el teléfono ya está registrado.
     *
     * @return el cliente nuevo o el existente
     * @throws IllegalArgumentException si falta algún dato
     */
    @Tool(description = "Identifica al cliente por su teléfono. Si ya existe lo devuelve sin duplicarlo; "
            + "si no, lo crea. Usar antes de crear un pedido.")
    @Transactional
    public ClienteDto registrarCliente(
            @ToolParam(description = "Nombre completo del cliente") String nombre,
            @ToolParam(description = "Teléfono del cliente, identifica al cliente") String telefono,
            @ToolParam(description = "Dirección habitual de entrega") String direccion) {
        String tel = obligatorio(telefono, "El teléfono es obligatorio");
        return clienteRepo.findByTelefono(tel)
                .map(ClienteDto::de)
                .orElseGet(() -> ClienteDto.de(clienteRepo.save(new Cliente(
                        obligatorio(nombre, "El nombre es obligatorio"), tel,
                        obligatorio(direccion, "La dirección es obligatoria")))));
    }

    /**
     * Crea un pedido vacío en {@code BORRADOR}.
     *
     * @param direccionEntrega dirección de entrega; vacía usa la del cliente
     * @throws IllegalArgumentException si el cliente no existe
     */
    @Tool(description = "Crea un pedido vacío en estado BORRADOR para un cliente. "
            + "Si la dirección de entrega viene vacía, se usa la del cliente.")
    @Transactional
    public PedidoDto crearPedido(
            @ToolParam(description = "Id del cliente (lo devuelve registrarCliente)") Long clienteId,
            @ToolParam(description = "Dirección de entrega (opcional)", required = false) String direccionEntrega) {
        Cliente cliente = clienteRepo.findById(requerirId(clienteId, "cliente"))
                .orElseThrow(() -> new IllegalArgumentException(NO_EXISTE_CLIENTE.formatted(clienteId)));
        String direccion = (direccionEntrega == null || direccionEntrega.isBlank())
                ? cliente.getDireccion() : direccionEntrega.trim();
        return PedidoDto.de(pedidoRepo.save(new Pedido(cliente, direccion, LocalDateTime.now())));
    }

    /**
     * Agrega unidades de un producto a un pedido en borrador; si ya estaba, suma la cantidad.
     * No descuenta stock: eso ocurre al confirmar.
     *
     * @throws IllegalArgumentException si el pedido no está en borrador, la cantidad no es positiva
     *                                  o no hay stock suficiente
     */
    @Tool(description = "Agrega un producto a un pedido en BORRADOR. Si el producto ya está en el pedido, "
            + "suma la cantidad. Falla si no hay stock suficiente.")
    @Transactional
    public PedidoDto agregarProducto(
            @ToolParam(description = "Id del pedido") Long pedidoId,
            @ToolParam(description = "Id del producto (lo devuelve buscarProductos)") Long productoId,
            @ToolParam(description = "Unidades a agregar, mayor que cero") Integer cantidad) {
        if (cantidad == null || cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero");
        }
        Pedido pedido = buscarPedido(pedidoId);
        exigirBorrador(pedido);
        Producto producto = productoRepo.findById(requerirId(productoId, "producto"))
                .orElseThrow(() -> new IllegalArgumentException(NO_EXISTE_PRODUCTO.formatted(productoId)));

        DetallePedido linea = pedido.getDetalles().stream()
                .filter(d -> d.getProducto().getId().equals(productoId))
                .findFirst()
                .orElse(null);
        int total = cantidad + (linea == null ? 0 : linea.getCantidad());
        exigirStock(producto, total);

        if (linea == null) {
            pedido.getDetalles().add(new DetallePedido(pedido, producto, cantidad, producto.getPrecio()));
        } else {
            linea.setCantidad(total);
        }
        return PedidoDto.de(pedidoRepo.save(pedido));
    }

    /**
     * Muestra un pedido con sus líneas, subtotales y total.
     *
     * @throws IllegalArgumentException si el pedido no existe
     */
    @Tool(description = "Muestra un pedido: estado, dirección, líneas con subtotales y total.")
    @Transactional(readOnly = true)
    public PedidoDto verPedido(@ToolParam(description = "Id del pedido") Long pedidoId) {
        return PedidoDto.de(buscarPedido(pedidoId));
    }

    /**
     * Confirma un pedido: revalida y descuenta el stock y fija el total, todo en una transacción.
     *
     * @throws IllegalArgumentException si no está en borrador, no tiene líneas o falta stock
     */
    @Tool(description = "Confirma un pedido en BORRADOR con al menos un producto: descuenta el stock y "
            + "calcula el total. Pedir confirmación explícita al cliente antes de usarla.")
    @Transactional
    public PedidoDto confirmarPedido(@ToolParam(description = "Id del pedido") Long pedidoId) {
        Pedido pedido = buscarPedido(pedidoId);
        exigirBorrador(pedido);
        if (pedido.getDetalles().isEmpty()) {
            throw new IllegalArgumentException("El pedido " + pedidoId + " no tiene productos");
        }
        // Se valida todo antes de descontar para no dejar el stock a medias.
        pedido.getDetalles().forEach(d -> exigirStock(d.getProducto(), d.getCantidad()));
        pedido.getDetalles().forEach(d -> d.getProducto().setStock(d.getProducto().getStock() - d.getCantidad()));

        PedidoDto dto = PedidoDto.de(pedido);
        pedido.setTotal(dto.total());
        pedido.setEstado(EstadoPedido.CONFIRMADO);
        return PedidoDto.de(pedidoRepo.save(pedido));
    }

    /**
     * Cancela un pedido en borrador o confirmado; si estaba confirmado, repone el stock.
     *
     * @throws IllegalArgumentException si el pedido no existe o ya estaba cancelado
     */
    @Tool(description = "Cancela un pedido en BORRADOR o CONFIRMADO. Si estaba confirmado, repone el stock. "
            + "Pedir confirmación explícita al cliente antes de usarla.")
    @Transactional
    public PedidoDto cancelarPedido(@ToolParam(description = "Id del pedido") Long pedidoId) {
        Pedido pedido = buscarPedido(pedidoId);
        if (pedido.getEstado() == EstadoPedido.CANCELADO) {
            throw new IllegalArgumentException("El pedido " + pedidoId + " ya está cancelado");
        }
        if (pedido.getEstado() == EstadoPedido.CONFIRMADO) {
            pedido.getDetalles().forEach(d -> d.getProducto().setStock(d.getProducto().getStock() + d.getCantidad()));
        }
        pedido.setEstado(EstadoPedido.CANCELADO);
        return PedidoDto.de(pedidoRepo.save(pedido));
    }

    private Pedido buscarPedido(Long pedidoId) {
        return pedidoRepo.findById(requerirId(pedidoId, "pedido"))
                .orElseThrow(() -> new IllegalArgumentException(NO_EXISTE_PEDIDO.formatted(pedidoId)));
    }

    private static void exigirBorrador(Pedido pedido) {
        if (pedido.getEstado() != EstadoPedido.BORRADOR) {
            throw new IllegalArgumentException("El pedido " + pedido.getId() + " está " + pedido.getEstado()
                    + "; solo se puede modificar o confirmar en BORRADOR");
        }
    }

    private static void exigirStock(Producto producto, int cantidad) {
        if (producto.getStock() < cantidad) {
            throw new IllegalArgumentException(
                    STOCK_INSUFICIENTE.formatted(producto.getNombre(), producto.getStock(), cantidad));
        }
    }

    private static Long requerirId(Long id, String entidad) {
        if (id == null) {
            throw new IllegalArgumentException("El id del " + entidad + " es obligatorio");
        }
        return id;
    }

    private static String obligatorio(String valor, String mensaje) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(mensaje);
        }
        return valor.trim();
    }
}
