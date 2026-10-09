package org.example.Models;

import java.util.ArrayList;

/**
 * Representa um hexágono regular com cálculo automático dos 6 vértices a partir do centro e raio.
 */
public class Hexagono extends FormaBase {
    private static final long serialVersionUID = 1L;

    private Ponto centroOriginal;
    private double raioOriginal;

    public Hexagono() {
        this(new Ponto(0.0, 0.0), 35.0);
    }

    public Hexagono(Ponto centro, double raio) {
        super();
        if (raio <= 0) {
            throw new IllegalArgumentException("O raio do hexágono deve ser estritamente positivo (r > 0).");
        }
        this.centroOriginal = (centro != null) ? centro.clonar() : new Ponto(0.0, 0.0);
        this.raioOriginal = raio;
        gerarVertices();
    }

    /**
     * Calcula os 6 vértices do hexágono regular com espaçamento angular de 60 graus (pi/3 radianos).
     */
    private void gerarVertices() {
        this.pontos = new ArrayList<>();
        double cx = centroOriginal.getX();
        double cy = centroOriginal.getY();
        double r = raioOriginal;

        for (int i = 0; i < 6; i++) {
            double angulo = i * (Math.PI / 3.0);
            double vx = cx + r * Math.cos(angulo);
            double vy = cy + r * Math.sin(angulo);
            this.pontos.add(new Ponto(vx, vy));
        }
    }

    public Ponto getCentroOriginal() {
        return centroOriginal;
    }

    public double getRaioOriginal() {
        return raioOriginal;
    }

    @Override
    public FormaGeometrica clonar() {
        Hexagono clone = new Hexagono(this.centroOriginal.clonar(), this.raioOriginal);
        copiarAtributosPara(clone);
        return clone;
    }

    @Override
    public String toString() {
        return String.format("Hexágono [Centroide: %s, Área: %.1f, Raio: %.1f]",
                getCentroide(), calcularArea(), raioOriginal);
    }
}
