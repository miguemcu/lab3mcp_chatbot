package com.example.labmcp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.labmcp.model.Tarea;
import com.example.labmcp.repository.TareaRepository;

/**
 * Pruebas unitarias de las actividades 15.1 a 15.3 en {@link TareaToolsService}, sin MySQL.
 */
@ExtendWith(MockitoExtension.class)
class TareaToolsServiceTest {

    @Mock
    private TareaRepository repo;

    private TareaToolsService service;

    @BeforeEach
    void setUp() {
        service = new TareaToolsService(repo);
    }

    @Test
    void contarTareas_devuelveElTotal() {
        when(repo.count()).thenReturn(3L);

        assertThat(service.contarTareas()).isEqualTo(3L);
    }

    @Test
    void listarCompletadas_soloDevuelveCompletadas() {
        Tarea hecha = new Tarea("Probar demo", "2026-10-25", true);
        when(repo.findByCompletado(true)).thenReturn(List.of(hecha));

        assertThat(service.listarCompletadas()).containsExactly(hecha);
    }

    @Test
    void crearTarea_fechaIsoValida_laGuarda() {
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Tarea creada = service.crearTarea("Entregar informe", " 2026-11-20 ");

        assertThat(creada.getFecha()).isEqualTo("2026-11-20");
    }

    @Test
    void crearTarea_fechaVacia_quedaSinFecha() {
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertThat(service.crearTarea("Entregar informe", "").getFecha()).isNull();
    }

    @Test
    void crearTarea_fechaConFormatoInvalido_lanzaErrorConFormatoEsperado() {
        assertThatThrownBy(() -> service.crearTarea("Entregar informe", "20/11/2026"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("yyyy-MM-dd");
        verifyNoInteractions(repo);
    }

    @Test
    void crearTarea_fechaInexistente_lanzaError() {
        assertThatThrownBy(() -> service.crearTarea("Entregar informe", "2026-02-30"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no es válida");
    }

    @Test
    void actualizarTarea_fechaInvalida_lanzaErrorSinGuardar() {
        assertThatThrownBy(() -> service.actualizarTarea(1L, null, "mañana"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("yyyy-MM-dd");
        verify(repo, never()).save(any());
    }

    @Test
    void actualizarTarea_fechaValida_laActualiza() {
        Tarea tarea = new Tarea("Revisar slides", "2026-10-24", false);
        when(repo.findById(1L)).thenReturn(Optional.of(tarea));
        when(repo.save(tarea)).thenReturn(tarea);

        assertThat(service.actualizarTarea(1L, null, "2026-12-01").getFecha()).isEqualTo("2026-12-01");
    }
}
