package org.example;

import org.example.Controller.HistoricoController;
import org.example.Models.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HistoricoControllerTest {

    @Test
    @DisplayName("Memento: Registrar estado, desfazer e refazer com cópias profundas")
    void testUndoRedoComCopiasProfundas() {
        HistoricoController historico = new HistoricoController();
        List<FormaGeometrica> formas = new ArrayList<>();

        // Estado 1: 1 círculo
        Circulo c1 = new Circulo(new Ponto(10, 10), 5.0);
        formas.add(c1);
        historico.registrarEstado(formas);

        // Estado 2: adiciona um quadrado (registra antes da translação seguinte)
        Quadrado q = new Quadrado(new Ponto(50, 50), 10.0);
        formas.add(q);
        historico.registrarEstado(formas);

        // Estado 3: move o círculo
        c1.transladar(20, 20);

        // Desfazer para Estado 2 (antes da translação do círculo)
        formas = historico.desfazer(formas);
        assertEquals(2, formas.size());
        Circulo cRestaurado = (Circulo) formas.get(0);
        assertEquals(10.0, cRestaurado.getCentro().getX());

        // Desfazer para Estado 1 (apenas 1 círculo)
        formas = historico.desfazer(formas);
        assertEquals(1, formas.size());

        // Refazer para Estado 2
        formas = historico.refazer(formas);
        assertEquals(2, formas.size());

        // Refazer para Estado 3
        formas = historico.refazer(formas);
        assertEquals(2, formas.size());
        Circulo cFinal = (Circulo) formas.get(0);
        assertEquals(30.0, cFinal.getCentro().getX());
    }

    @Test
    @DisplayName("Memento: Modificações externas não devem corromper histórico salvo")
    void testIsolamentoDoHistorico() {
        HistoricoController historico = new HistoricoController();
        List<FormaGeometrica> formas = new ArrayList<>();

        Poligono p = new Poligono();
        p.adicionarPonto(new Ponto(0, 0));
        p.adicionarPonto(new Ponto(10, 0));
        p.adicionarPonto(new Ponto(10, 10));
        formas.add(p);

        historico.registrarEstado(formas);

        // Modifica objeto e lista diretamente
        p.getPontos().get(0).mover(100, 100);
        formas.clear();

        // O estado salvo no histórico deve permanecer intacto
        List<FormaGeometrica> restaurado = historico.desfazer(new ArrayList<>());
        assertEquals(1, restaurado.size());
        assertEquals(0.0, restaurado.get(0).getPontos().get(0).getX());
    }
}
