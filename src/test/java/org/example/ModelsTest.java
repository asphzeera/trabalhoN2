package org.example;

import org.example.Models.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ModelsTest {

    @Test
    @DisplayName("Ponto: operações básicas, distância e clonagem")
    void testPonto() {
        Ponto p1 = new Ponto(10.0, 20.0);
        assertEquals(10.0, p1.getX());
        assertEquals(20.0, p1.getY());

        p1.mover(5.0, -10.0);
        assertEquals(15.0, p1.getX());
        assertEquals(10.0, p1.getY());

        Ponto p2 = new Ponto(15.0, 14.0);
        assertEquals(4.0, p1.distancia(p2), 0.0001);

        Ponto clone = p1.clonar();
        assertNotSame(p1, clone);
        assertEquals(p1, clone);

        clone.mover(100.0, 100.0);
        assertNotEquals(p1.getX(), clone.getX());
    }

    @Test
    @DisplayName("Polígono: Área pela fórmula de Gauss e Perímetro Euclidiano")
    void testPoligonoAreaEPerimetro() {
        Poligono triangulo = new Poligono();
        triangulo.adicionarPonto(new Ponto(0.0, 0.0));
        triangulo.adicionarPonto(new Ponto(4.0, 0.0));
        triangulo.adicionarPonto(new Ponto(0.0, 3.0));

        assertTrue(triangulo.isValido());
        assertEquals(3, triangulo.getQuantidadePontos());

        // Triângulo retângulo de base 4 e altura 3 -> Área = 6.0
        assertEquals(6.0, triangulo.calcularArea(), 0.0001);

        // Perímetro = 4 + 5 + 3 = 12.0
        assertEquals(12.0, triangulo.calcularPerimetro(), 0.0001);

        // Teste de contenção de ponto
        assertTrue(triangulo.contemPonto(1.0, 1.0));
        assertFalse(triangulo.contemPonto(10.0, 10.0));
    }

    @Test
    @DisplayName("Polígono: Mínimo 3 pontos para validação")
    void testPoligonoMinimoTresPontos() {
        Poligono poli = new Poligono();
        assertFalse(poli.isValido());

        poli.adicionarPonto(new Ponto(0, 0));
        assertFalse(poli.isValido());

        poli.adicionarPonto(new Ponto(10, 0));
        assertFalse(poli.isValido());

        poli.adicionarPonto(new Ponto(10, 10));
        assertTrue(poli.isValido());
    }

    @Test
    @DisplayName("Círculo: Raio estritamente positivo (r > 0), área e perímetro")
    void testCirculoValidacaoEArea() {
        assertThrows(IllegalArgumentException.class, () -> new Circulo(new Ponto(0, 0), 0.0));
        assertThrows(IllegalArgumentException.class, () -> new Circulo(new Ponto(0, 0), -5.0));

        Circulo c = new Circulo(new Ponto(10, 10), 5.0);
        assertEquals(Math.PI * 25.0, c.calcularArea(), 0.0001);
        assertEquals(2.0 * Math.PI * 5.0, c.calcularPerimetro(), 0.0001);

        assertTrue(c.contemPonto(12, 12));
        assertFalse(c.contemPonto(30, 30));
    }

    @Test
    @DisplayName("Quadrado: Cálculo automático de vértices")
    void testQuadradoVertices() {
        // Centro (50, 50) e semi-lado 10 -> Lado = 20 -> Área = 400
        Quadrado q = new Quadrado(new Ponto(50, 50), 10.0);
        assertEquals(4, q.getQuantidadePontos());
        assertEquals(400.0, q.calcularArea(), 0.0001);
        assertEquals(80.0, q.calcularPerimetro(), 0.0001);
    }

    @Test
    @DisplayName("Hexágono: Cálculo automático de 6 vértices")
    void testHexagonoVertices() {
        Hexagono h = new Hexagono(new Ponto(100, 100), 20.0);
        assertEquals(6, h.getQuantidadePontos());
        assertTrue(h.calcularArea() > 0);
        assertTrue(h.calcularPerimetro() > 0);
    }

    @Test
    @DisplayName("Prototype Pattern: Verificação de clonagem independente de formas")
    void testPrototypeClone() {
        Poligono original = new Poligono();
        original.adicionarPonto(new Ponto(10, 10));
        original.adicionarPonto(new Ponto(20, 10));
        original.adicionarPonto(new Ponto(20, 20));
        original.setCorPreenchimentoHex("#FF0000");
        original.setGroupId("grupo-1");

        FormaGeometrica clone = original.clonar();

        assertNotSame(original, clone);
        assertEquals(original.getCorPreenchimentoHex(), clone.getCorPreenchimentoHex());
        assertEquals(original.getGroupId(), clone.getGroupId());
        assertEquals(original.getQuantidadePontos(), clone.getQuantidadePontos());

        // Modificações no clone não devem alterar o original
        clone.transladar(50, 50);
        clone.setCorPreenchimentoHex("#00FF00");

        assertEquals(10.0, original.getPontos().get(0).getX());
        assertEquals(60.0, clone.getPontos().get(0).getX());
        assertEquals("#FF0000", original.getCorPreenchimentoHex());
        assertEquals("#00FF00", clone.getCorPreenchimentoHex());
    }
}
