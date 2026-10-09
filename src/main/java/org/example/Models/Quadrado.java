package org.example.Models;

import java.util.ArrayList;

/**
 * Representa um quadrado regular com cálculo automático de vértices a partir do centro e raio/semi-lado.
 */
public class Quadrado extends FormaBase {
    private static final long serialVersionUID = 1L;

    private Ponto centroOriginal;
    private double raioOriginal;

    public Quadrado() {
        this(new Ponto(0.0, 0.0), 30.0);
    }

    public Quadrado(Ponto centro, double raio) {
        super();
        if (raio <= 0) {
            throw new IllegalArgumentException("O raio/semi-lado do quadrado deve ser estritamente positivo (r > 0).");
        }
        this.centroOriginal = (centro != null) ? centro.clonar() : new Ponto(0.0, 0.0);
        this.raioOriginal = raio;
        gerarVertices();
    }

    /**
     * Calcula os 4 vértices do quadrado alinhado aos eixos automaticamente.
     */
    private void gerarVertices() {
        this.pontos = new ArrayList<>();
        double cx = centroOriginal.getX();
        double cy = centroOriginal.getY();
        double r = raioOriginal;

        this.pontos.add(new Ponto(cx - r, cy - r));
        this.pontos.add(new Ponto(cx + r, cy - r));
        this.pontos.add(new Ponto(cx + r, cy + r));
        this.pontos.add(new Ponto(cx - r, cy + r));
    }

    public Ponto getCentroOriginal() {
        return centroOriginal;
    }

    public double getRaioOriginal() {
        return raioOriginal;
    }

    @Override
    public FormaGeometrica clonar() {
        Quadrado clone = new Quadrado(this.centroOriginal.clonar(), this.raioOriginal);
        copiarAtributosPara(clone);
        return clone;
    }

    @Override
    public String toString() {
        return String.format("Quadrado [Centroide: %s, Área: %.1f, Lado: %.1f]",
                getCentroide(), calcularArea(), raioOriginal * 2.0);
    }
}
