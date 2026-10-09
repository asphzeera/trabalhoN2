package org.example.Models;

/**
 * Representa uma forma circular com centro e raio estritamente positivo (r > 0).
 */
public class Circulo extends FormaBase {
    private static final long serialVersionUID = 1L;

    private Ponto centro;
    private double raio;

    public Circulo() {
        this(new Ponto(0.0, 0.0), 30.0);
    }

    public Circulo(Ponto centro, double raio) {
        super();
        if (raio <= 0) {
            throw new IllegalArgumentException("O raio do círculo deve ser estritamente positivo (r > 0). Valor informado: " + raio);
        }
        this.centro = (centro != null) ? centro.clonar() : new Ponto(0.0, 0.0);
        this.raio = raio;
        atualizarPontosInternos();
    }

    private void atualizarPontosInternos() {
        this.pontos.clear();
        this.pontos.add(this.centro);
    }

    public Ponto getCentro() {
        return centro;
    }

    public void setCentro(Ponto centro) {
        this.centro = (centro != null) ? centro : new Ponto(0.0, 0.0);
        atualizarPontosInternos();
    }

    public double getRaio() {
        return raio;
    }

    public void setRaio(double raio) {
        if (raio <= 0) {
            throw new IllegalArgumentException("O raio do círculo deve ser estritamente positivo (r > 0).");
        }
        this.raio = raio;
    }

    @Override
    public double calcularArea() {
        return Math.PI * raio * raio;
    }

    @Override
    public double calcularPerimetro() {
        return 2.0 * Math.PI * raio;
    }

    @Override
    public Ponto getCentroide() {
        return centro.clonar();
    }

    @Override
    public boolean contemPonto(double px, double py) {
        return centro.distancia(new Ponto(px, py)) <= raio;
    }

    @Override
    public void transladar(double dx, double dy) {
        centro.mover(dx, dy);
    }

    @Override
    public void escalar(double fator) {
        double absFator = Math.abs(fator);
        if (absFator > 0.0001) {
            this.raio = Math.max(1.0, this.raio * absFator);
        }
    }

    @Override
    public void rotacionar(double anguloRadianos) {
        // Círculo individual é simétrico em relação ao próprio centroide.
    }

    @Override
    public void cisalhar(double shx, double shy) {
        // Para transformações compostas que mantenham o centroide estável
    }

    @Override
    public FormaGeometrica clonar() {
        Circulo clone = new Circulo(this.centro.clonar(), this.raio);
        copiarAtributosPara(clone);
        return clone;
    }

    @Override
    public String toString() {
        return String.format("Círculo [Centro: %s, Raio: %.1f, Área: %.1f]", centro, raio, calcularArea());
    }
}
