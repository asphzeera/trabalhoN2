package org.example.Models;

/**
 * Representa um polígono genérico definido por 3 ou mais vértices arbitrários.
 */
public class Poligono extends FormaBase {
    private static final long serialVersionUID = 1L;

    public Poligono() {
        super();
    }

    /**
     * Valida se o polígono possui a quantidade mínima exigida de vértices (mínimo 3).
     */
    public boolean isValido() {
        return getQuantidadePontos() >= 3;
    }

    @Override
    public FormaGeometrica clonar() {
        Poligono clone = new Poligono();
        copiarAtributosPara(clone);
        return clone;
    }

    @Override
    public String toString() {
        return String.format("Polígono [Vértices: %d, Área: %.1f]", getQuantidadePontos(), calcularArea());
    }
}
