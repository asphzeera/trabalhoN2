package org.example.Models;

import java.io.Serializable;
import java.util.Objects;

/**
 * Representa uma coordenada bidimensional (x, y) no espaço cartesiano do canvas.
 */
public class Ponto implements Serializable {
    private static final long serialVersionUID = 1L;

    private double x;
    private double y;

    public Ponto() {
        this(0.0, 0.0);
    }

    public Ponto(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    /**
     * Translada o ponto deslocando suas coordenadas por dx e dy.
     */
    public void mover(double dx, double dy) {
        this.x += dx;
        this.y += dy;
    }

    /**
     * Calcula a distância euclidiana até outro ponto.
     */
    public double distancia(Ponto outro) {
        if (outro == null) {
            return 0.0;
        }
        double dx = this.x - outro.x;
        double dy = this.y - outro.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * Retorna uma nova instância independente do Ponto (Prototype).
     */
    public Ponto clonar() {
        return new Ponto(this.x, this.y);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ponto ponto = (Ponto) o;
        return Double.compare(ponto.x, x) == 0 && Double.compare(ponto.y, y) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return String.format("(%.2f, %.2f)", x, y);
    }
}
