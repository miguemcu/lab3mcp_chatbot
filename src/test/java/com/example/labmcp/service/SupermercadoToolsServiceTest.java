package com.example.labmcp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

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
 * Pruebas unitarias de {@link SupermercadoToolsService}, con repositorios simulados (sin MySQL).
 */
@ExtendWith(MockitoExtension.class)
class SupermercadoToolsServiceTest {

    @Mock
    private ProductoRepository productoRepo;
    @Mock
    private ClienteRepository clienteRepo;
    @Mock
    private PedidoRepository pedidoRepo;

    private SupermercadoToolsService service;
    private Cliente cliente;
    private Producto leche;

    @BeforeEach
    void setUp() {
        service = new SupermercadoToolsService(productoRepo, clienteRepo, pedidoRepo);
        cliente = conId(new Cliente("Ana Gómez", "3001234567", "Calle 10"), 1L);
        leche = conId(new Producto("Leche entera 1 L", "Lácteos", new BigDecimal("4500.00"), 10), 7L);
    }

    @Test
    void buscarProductos_devuelveCoincidencias() {
        when(productoRepo.findByNombreContainingIgnoreCaseOrCategoriaContainingIgnoreCase("leche", "leche"))
                .thenReturn(List.of(leche));

        List<ProductoDto> resultado = service.buscarProductos("  leche ");

        assertThat(resultado).containsExactly(
                new ProductoDto(7L, "Leche entera 1 L", "Lácteos", new BigDecimal("4500.00"), 10));
    }

    @Test
    void registrarCliente_nuevo_loCrea() {
        when(clienteRepo.findByTelefono("3200000000")).thenReturn(Optional.empty());
        when(clienteRepo.save(any())).thenAnswer(inv -> conId(inv.getArgument(0), 5L));

        ClienteDto resultado = service.registrarCliente("Luis", "3200000000", "Carrera 1");

        assertThat(resultado).isEqualTo(new ClienteDto(5L, "Luis", "3200000000", "Carrera 1"));
    }

    @Test
    void registrarCliente_telefonoExistente_noDuplica() {
        when(clienteRepo.findByTelefono("3001234567")).thenReturn(Optional.of(cliente));

        ClienteDto resultado = service.registrarCliente("Otro nombre", "3001234567", "Otra dirección");

        assertThat(resultado.id()).isEqualTo(1L);
        verify(clienteRepo, never()).save(any());
    }

    @Test
    void crearPedido_sinDireccion_usaLaDelClienteYQuedaEnBorrador() {
        when(clienteRepo.findById(1L)).thenReturn(Optional.of(cliente));
        when(pedidoRepo.save(any())).thenAnswer(inv -> conId(inv.getArgument(0), 20L));

        PedidoDto resultado = service.crearPedido(1L, " ");

        assertThat(resultado.id()).isEqualTo(20L);
        assertThat(resultado.estado()).isEqualTo(EstadoPedido.BORRADOR);
        assertThat(resultado.direccionEntrega()).isEqualTo("Calle 10");
    }

    @Test
    void crearPedido_clienteInexistente_lanzaError() {
        when(clienteRepo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.crearPedido(99L, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("No existe el cliente 99");
    }

    @Test
    void agregarProducto_productoRepetido_sumaLaCantidad() {
        Pedido pedido = pedido(EstadoPedido.BORRADOR, 2);
        prepararAgregar(pedido);

        PedidoDto resultado = service.agregarProducto(20L, 7L, 3);

        assertThat(resultado.lineas()).hasSize(1);
        assertThat(resultado.lineas().get(0).cantidad()).isEqualTo(5);
        assertThat(resultado.total()).isEqualByComparingTo("22500");
        assertThat(leche.getStock()).isEqualTo(10);
    }

    @Test
    void agregarProducto_stockInsuficiente_lanzaError() {
        when(pedidoRepo.findById(20L)).thenReturn(Optional.of(pedido(EstadoPedido.BORRADOR, 0)));
        when(productoRepo.findById(7L)).thenReturn(Optional.of(leche));

        assertThatThrownBy(() -> service.agregarProducto(20L, 7L, 11))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Stock insuficiente");
    }

    @Test
    void agregarProducto_pedidoConfirmado_lanzaError() {
        when(pedidoRepo.findById(20L)).thenReturn(Optional.of(pedido(EstadoPedido.CONFIRMADO, 2)));

        assertThatThrownBy(() -> service.agregarProducto(20L, 7L, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("solo se puede modificar o confirmar en BORRADOR");
    }

    @Test
    void agregarProducto_cantidadCero_lanzaError() {
        assertThatThrownBy(() -> service.agregarProducto(20L, 7L, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("La cantidad debe ser mayor que cero");
    }

    @Test
    void verPedido_calculaSubtotalesYTotal() {
        when(pedidoRepo.findById(20L)).thenReturn(Optional.of(pedido(EstadoPedido.BORRADOR, 3)));

        PedidoDto resultado = service.verPedido(20L);

        assertThat(resultado.lineas().get(0).subtotal()).isEqualByComparingTo("13500");
        assertThat(resultado.total()).isEqualByComparingTo("13500");
    }

    @Test
    void confirmarPedido_descuentaStockYCalculaTotal() {
        Pedido pedido = pedido(EstadoPedido.BORRADOR, 4);
        when(pedidoRepo.findById(20L)).thenReturn(Optional.of(pedido));
        when(pedidoRepo.save(pedido)).thenReturn(pedido);

        PedidoDto resultado = service.confirmarPedido(20L);

        assertThat(resultado.estado()).isEqualTo(EstadoPedido.CONFIRMADO);
        assertThat(pedido.getTotal()).isEqualByComparingTo("18000");
        assertThat(leche.getStock()).isEqualTo(6);
    }

    @Test
    void confirmarPedido_sinLineas_lanzaError() {
        when(pedidoRepo.findById(20L)).thenReturn(Optional.of(pedido(EstadoPedido.BORRADOR, 0)));

        assertThatThrownBy(() -> service.confirmarPedido(20L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El pedido 20 no tiene productos");
    }

    @Test
    void confirmarPedido_stockAgotadoDespuesDeAgregar_lanzaErrorSinDescontar() {
        Pedido pedido = pedido(EstadoPedido.BORRADOR, 4);
        leche.setStock(3);
        when(pedidoRepo.findById(20L)).thenReturn(Optional.of(pedido));

        assertThatThrownBy(() -> service.confirmarPedido(20L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Stock insuficiente");
        assertThat(leche.getStock()).isEqualTo(3);
        assertThat(pedido.getEstado()).isEqualTo(EstadoPedido.BORRADOR);
    }

    @Test
    void cancelarPedido_borrador_noTocaElStock() {
        Pedido pedido = pedido(EstadoPedido.BORRADOR, 4);
        when(pedidoRepo.findById(20L)).thenReturn(Optional.of(pedido));
        when(pedidoRepo.save(pedido)).thenReturn(pedido);

        PedidoDto resultado = service.cancelarPedido(20L);

        assertThat(resultado.estado()).isEqualTo(EstadoPedido.CANCELADO);
        assertThat(leche.getStock()).isEqualTo(10);
    }

    @Test
    void cancelarPedido_confirmado_reponeElStock() {
        Pedido pedido = pedido(EstadoPedido.CONFIRMADO, 4);
        leche.setStock(6);
        when(pedidoRepo.findById(20L)).thenReturn(Optional.of(pedido));
        when(pedidoRepo.save(pedido)).thenReturn(pedido);

        PedidoDto resultado = service.cancelarPedido(20L);

        assertThat(resultado.estado()).isEqualTo(EstadoPedido.CANCELADO);
        assertThat(leche.getStock()).isEqualTo(10);
    }

    @Test
    void cancelarPedido_yaCancelado_lanzaError() {
        when(pedidoRepo.findById(20L)).thenReturn(Optional.of(pedido(EstadoPedido.CANCELADO, 1)));

        assertThatThrownBy(() -> service.cancelarPedido(20L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El pedido 20 ya está cancelado");
    }

    /** Pedido 20 del cliente de prueba, con una línea de leche si {@code cantidadLeche > 0}. */
    private Pedido pedido(EstadoPedido estado, int cantidadLeche) {
        Pedido pedido = conId(new Pedido(cliente, "Calle 10", LocalDateTime.now()), 20L);
        pedido.setEstado(estado);
        if (cantidadLeche > 0) {
            pedido.getDetalles().add(new DetallePedido(pedido, leche, cantidadLeche, leche.getPrecio()));
        }
        return pedido;
    }

    private void prepararAgregar(Pedido pedido) {
        when(pedidoRepo.findById(20L)).thenReturn(Optional.of(pedido));
        when(productoRepo.findById(7L)).thenReturn(Optional.of(leche));
        when(pedidoRepo.save(pedido)).thenReturn(pedido);
    }

    /** Asigna el id que en producción generaría MySQL. */
    private static <T> T conId(T entidad, Long id) {
        ReflectionTestUtils.setField(entidad, "id", id);
        return entidad;
    }
}
