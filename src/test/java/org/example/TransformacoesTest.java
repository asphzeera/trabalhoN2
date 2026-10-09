package org.example;

import org.example.Models.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TransformacoesTest {

    @Test
    @DisplayName("Transformação: Translação de polígono")
    void testTranslacao() {
        Poligono p = new Poligono();
        p.adicionarPonto(new Ponto(10, 10));
        p.adicionarPonto(new Ponto(20, 10));
        p.adicionarPonto(new Ponto(20, 20));

        p.transladar(5, -2);

        assertEquals(15.0, p.getPontos().get(0).getX());
        assertEquals(8.0, p.getPontos().get(0).getY());
    }

    @Test
    @DisplayName("Transformação: Escala em relação ao centroide")
    void testEscala() {
        Quadrado q = new Quadrado(new Ponto(100, 100), 20.0);
        double areaInicial = q.calcularArea();

        // Escalar por fator 2.0 deve quadruplicar a área (fator^2)
        q.escalar(2.0);

        assertEquals(areaInicial * 4.0, q.calcularArea(), 0.001);
        assertEquals(100.0, q.getCentroide().getX(), 0.001);
        assertEquals(100.0, q.getCentroide().getY(), 0.001);
    }

    @Test
    @DisplayName("Transformação: Rotação em torno do centroide")
    void testRotacao() {
        Quadrado q = new Quadrado(new Ponto(50, 50), 10.0);
        double areaInicial = q.calcularArea();

        // Rotação em 90 graus (PI / 2) preserva a área e o centroide
        q.rotacionar(Math.PI / 2.0);

        assertEquals(areaInicial, q.calcularArea(), 0.001);
        assertEquals(50.0, q.getCentroide().getX(), 0.001);
        assertEquals(50.0, q.getCentroide().getY(), 0.001);
    }

    @Test
    @DisplayName("Transformação: Cisalhamento (Shear)")
    void testCisalhamento() {
        Quadrado q = new Quadrado(new Ponto(50, 50), 10.0);
        double areaInicial = q.calcularArea();

        q.cisalhar(0.5, 0.0);

        // Cisalhamento puro preserva o centroide e a área
        assertEquals(50.0, q.getCentroide().getX(), 0.001);
        assertEquals(50.0, q.getCentroide().getY(), 0.001);
        assertEquals(areaInicial, q.calcularArea(), 0.001);
    }
}
