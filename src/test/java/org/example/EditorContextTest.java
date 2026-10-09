package org.example;

import org.example.Controller.EditorContext;
import org.example.Models.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EditorContextTest {

    @Test
    @DisplayName("Composite Pattern: Agrupamento sob groupId e seleção conjunta")
    void testAgrupamentoESelecaoComposite() {
        EditorContext context = new EditorContext();

        Circulo c1 = new Circulo(new Ponto(10, 10), 5.0);
        Quadrado q1 = new Quadrado(new Ponto(50, 50), 10.0);
        Hexagono h1 = new Hexagono(new Ponto(100, 100), 15.0);

        context.getFormas().add(c1);
        context.getFormas().add(q1);
        context.getFormas().add(h1);

        // Seleciona c1 e q1 e agrupa
        context.selecionarForma(c1, true);
        context.selecionarForma(q1, true);
        assertEquals(2, context.getSelecionados().size());

        context.agruparSelecionados();

        assertNotNull(c1.getGroupId());
        assertEquals(c1.getGroupId(), q1.getGroupId());
        assertNull(h1.getGroupId());

        // Limpa seleção e seleciona apenas c1: q1 deve ser automaticamente selecionado junto (Composite)
        context.limparSelecao();
        assertEquals(0, context.getSelecionados().size());

        context.selecionarForma(c1, false);
        assertEquals(2, context.getSelecionados().size());
        assertTrue(context.getSelecionados().contains(c1));
        assertTrue(context.getSelecionados().contains(q1));

        // Desagrupar
        context.desagruparSelecionados();
        assertNull(c1.getGroupId());
        assertNull(q1.getGroupId());
    }

    @Test
    @DisplayName("Camadas: Trazer para frente e enviar para trás (Z-index)")
    void testOrdenacaoCamadas() {
        EditorContext context = new EditorContext();

        Circulo c1 = new Circulo(new Ponto(10, 10), 5.0);
        Quadrado q1 = new Quadrado(new Ponto(50, 50), 10.0);
        Hexagono h1 = new Hexagono(new Ponto(100, 100), 15.0);

        context.getFormas().add(c1);
        context.getFormas().add(q1);
        context.getFormas().add(h1);

        // c1 está na posição 0. Seleciona c1 e traz para a frente (topo da lista)
        context.selecionarForma(c1, false);
        context.trazerParaFrente();

        assertEquals(h1, context.getFormas().get(1));
        assertEquals(c1, context.getFormas().get(2)); // Último elemento (frente)

        // Envia c1 para trás (início da lista)
        context.enviarParaTras();
        assertEquals(c1, context.getFormas().get(0));
    }

    @Test
    @DisplayName("Snap to Grid: Aplicação de alinhamento magnético às coordenadas")
    void testSnapToGrid() {
        EditorContext context = new EditorContext();
        context.setGridSize(20.0);

        // Snap desativado
        context.setGridSnappingHabilitado(false);
        assertEquals(23.7, context.aplicarSnap(23.7), 0.001);

        // Snap ativado
        context.setGridSnappingHabilitado(true);
        assertEquals(20.0, context.aplicarSnap(23.7), 0.001);
        assertEquals(40.0, context.aplicarSnap(31.2), 0.001);
        assertEquals(0.0, context.aplicarSnap(8.9), 0.001);
    }
}
