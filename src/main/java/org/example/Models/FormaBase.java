package org.example.Models;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe base abstrata para formas geométricas, fornecendo campos comuns
 * e implementação padrão de transformações geométricas e cálculos baseados em vértices.
 */
public abstract class FormaBase implements FormaGeometrica {
    private static final long serialVersionUID = 1L;

    protected List<Ponto> pontos;
    protected String corPreenchimentoHex;
    protected String corBordaHex;
    protected double espessuraBorda;
    protected String groupId;

    public FormaBase() {
        this.pontos = new ArrayList<>();
        this.corPreenchimentoHex = "#80C5DE";
        this.corBordaHex = "#2C3E50";
        this.espessuraBorda = 2.0;
        this.groupId = null;
    }

    @Override
    public void adicionarPonto(Ponto p) {
        if (p != null) {
            this.pontos.add(p);
        }
    }

    @Override
    public List<Ponto> getPontos() {
        return this.pontos;
    }

    public void setPontos(List<Ponto> pontos) {
        this.pontos = (pontos != null) ? pontos : new ArrayList<>();
    }

    @Override
    public int getQuantidadePontos() {
        return this.pontos.size();
    }

    @Override
    public String getCorPreenchimentoHex() {
        return this.corPreenchimentoHex;
    }

    @Override
    public void setCorPreenchimentoHex(String hex) {
        this.corPreenchimentoHex = hex;
    }

    @Override
    public String getCorBordaHex() {
        return this.corBordaHex;
    }

    @Override
    public void setCorBordaHex(String hex) {
        this.corBordaHex = hex;
    }

    @Override
    public double getEspessuraBorda() {
        return this.espessuraBorda;
    }

    @Override
    public void setEspessuraBorda(double espessura) {
        this.espessuraBorda = Math.max(0.5, espessura);
    }

    @Override
    public String getGroupId() {
        return this.groupId;
    }

    @Override
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    /**
     * Calcula a área do polígono utilizando a Fórmula do Cadarço de Gauss (Shoelace).
     * A = 1/2 * | soma_{i=0..n-1} (x_i * y_{i+1} - x_{i+1} * y_i) |
     */
    @Override
    public double calcularArea() {
        int n = pontos.size();
        if (n < 3) {
            return 0.0;
        }

        double soma = 0.0;
        for (int i = 0; i < n; i++) {
            Ponto atual = pontos.get(i);
            Ponto proximo = pontos.get((i + 1) % n);
            soma += (atual.getX() * proximo.getY() - proximo.getX() * atual.getY());
        }

        return Math.abs(soma) / 2.0;
    }

    /**
     * Calcula o perímetro como a soma das distâncias euclidianas entre vértices consecutivos.
     */
    @Override
    public double calcularPerimetro() {
        int n = pontos.size();
        if (n < 2) {
            return 0.0;
        }

        double perimetro = 0.0;
        for (int i = 0; i < n; i++) {
            Ponto atual = pontos.get(i);
            Ponto proximo = pontos.get((i + 1) % n);
            perimetro += atual.distancia(proximo);
        }

        return perimetro;
    }

    /**
     * Calcula o centroide aritmético dos vértices da forma.
     */
    @Override
    public Ponto getCentroide() {
        if (pontos.isEmpty()) {
            return new Ponto(0.0, 0.0);
        }

        double somaX = 0.0;
        double somaY = 0.0;
        for (Ponto p : pontos) {
            somaX += p.getX();
            somaY += p.getY();
        }

        return new Ponto(somaX / pontos.size(), somaY / pontos.size());
    }

    /**
     * Determina se o ponto (px, py) está contido no polígono usando o algoritmo Ray Casting.
     */
    @Override
    public boolean contemPonto(double px, double py) {
        int n = pontos.size();
        if (n < 3) {
            return false;
        }

        boolean dentro = false;
        for (int i = 0, j = n - 1; i < n; j = i++) {
            double xi = pontos.get(i).getX();
            double yi = pontos.get(i).getY();
            double xj = pontos.get(j).getX();
            double yj = pontos.get(j).getY();

            boolean intersecao = ((yi > py) != (yj > py))
                    && (px < (xj - xi) * (py - yi) / (yj - yi) + xi);
            if (intersecao) {
                dentro = !dentro;
            }
        }
        return dentro;
    }

    /**
     * Translada todos os pontos somando dx e dy.
     */
    @Override
    public void transladar(double dx, double dy) {
        for (Ponto p : pontos) {
            p.mover(dx, dy);
        }
    }

    /**
     * Escala a forma multiplicando a distância de cada ponto ao centroide pelo fator.
     */
    @Override
    public void escalar(double fator) {
        if (pontos.isEmpty() || Double.compare(fator, 0.0) == 0) {
            return;
        }
        Ponto c = getCentroide();
        for (Ponto p : pontos) {
            double novoX = c.getX() + (p.getX() - c.getX()) * fator;
            double novoY = c.getY() + (p.getY() - c.getY()) * fator;
            p.setX(novoX);
            p.setY(novoY);
        }
    }

    /**
     * Rotaciona todos os pontos em torno do centroide pelo ângulo especificado em radianos.
     */
    @Override
    public void rotacionar(double anguloRadianos) {
        if (pontos.isEmpty() || Double.compare(anguloRadianos, 0.0) == 0) {
            return;
        }
        Ponto c = getCentroide();
        double cos = Math.cos(anguloRadianos);
        double sin = Math.sin(anguloRadianos);

        for (Ponto p : pontos) {
            double dx = p.getX() - c.getX();
            double dy = p.getY() - c.getY();
            double novoX = c.getX() + (dx * cos - dy * sin);
            double novoY = c.getY() + (dx * sin + dy * cos);
            p.setX(novoX);
            p.setY(novoY);
        }
    }

    /**
     * Aplica cisalhamento (shear) aos pontos em torno do centroide.
     */
    @Override
    public void cisalhar(double shx, double shy) {
        if (pontos.isEmpty()) {
            return;
        }
        Ponto c = getCentroide();
        for (Ponto p : pontos) {
            double dx = p.getX() - c.getX();
            double dy = p.getY() - c.getY();
            double novoX = c.getX() + dx + shx * dy;
            double novoY = c.getY() + shy * dx + dy;
            p.setX(novoX);
            p.setY(novoY);
        }
    }

    /**
     * Copia os atributos base para outra forma (auxiliar do Prototype pattern).
     */
    protected void copiarAtributosPara(FormaBase destino) {
        destino.corPreenchimentoHex = this.corPreenchimentoHex;
        destino.corBordaHex = this.corBordaHex;
        destino.espessuraBorda = this.espessuraBorda;
        destino.groupId = this.groupId;
        destino.pontos = new ArrayList<>();
        for (Ponto p : this.pontos) {
            destino.pontos.add(p.clonar());
        }
    }
}
