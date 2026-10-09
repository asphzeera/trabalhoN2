package org.example.Controller;

import javafx.scene.input.MouseEvent;
import org.example.Models.*;

/**
 * Ferramenta responsável por inserir formas pré-definidas (Círculo, Quadrado, Hexágono)
 * interativamente através de clique ou arrasto.
 */
public class FerramentaInserirForma implements Ferramenta {

    public enum TipoForma {
        CIRCULO,
        QUADRADO,
        HEXAGONO
    }

    private Ponto centroInsercao;
    private TipoForma tipoForma;

    public FerramentaInserirForma() {
        this(TipoForma.CIRCULO);
    }

    public FerramentaInserirForma(TipoForma tipoForma) {
        this.tipoForma = (tipoForma != null) ? tipoForma : TipoForma.CIRCULO;
    }

    public TipoForma getTipoForma() {
        return tipoForma;
    }

    public void setTipoForma(TipoForma tipoForma) {
        this.tipoForma = tipoForma;
    }

    public Ponto getCentroInsercao() {
        return centroInsercao;
    }

    private FormaGeometrica criarForma(Ponto centro, double raio, EditorContext context) {
        FormaGeometrica f;
        switch (tipoForma) {
            case QUADRADO:
                f = new Quadrado(centro, raio);
                break;
            case HEXAGONO:
                f = new Hexagono(centro, raio);
                break;
            case CIRCULO:
            default:
                f = new Circulo(centro, raio);
                break;
        }
        f.setCorPreenchimentoHex(context.getCorPreenchimentoAtual());
        f.setCorBordaHex(context.getCorBordaAtual());
        f.setEspessuraBorda(context.getEspessuraBordaAtual());
        return f;
    }

    @Override
    public void aoPressionarMouse(MouseEvent event, EditorContext context) {
        double x = context.aplicarSnap(event.getX());
        double y = context.aplicarSnap(event.getY());
        centroInsercao = new Ponto(x, y);

        // Inicializa prévia com raio padrão
        FormaGeometrica previa = criarForma(centroInsercao, 5.0, context);
        context.setFormaEmAndamento(previa);
    }

    @Override
    public void aoArrastarMouse(MouseEvent event, EditorContext context) {
        if (centroInsercao == null) {
            return;
        }
        double x = context.aplicarSnap(event.getX());
        double y = context.aplicarSnap(event.getY());
        double raio = Math.max(5.0, centroInsercao.distancia(new Ponto(x, y)));

        FormaGeometrica previa = criarForma(centroInsercao, raio, context);
        context.setFormaEmAndamento(previa);
    }

    @Override
    public void aoSoltarMouse(MouseEvent event, EditorContext context) {
        if (centroInsercao == null) {
            return;
        }
        double x = context.aplicarSnap(event.getX());
        double y = context.aplicarSnap(event.getY());
        double raio = centroInsercao.distancia(new Ponto(x, y));

        if (raio < 8.0) {
            raio = 35.0; // Raio padrão caso tenha sido apenas um clique
        }

        context.registrarHistorico();
        FormaGeometrica formaFinal = criarForma(centroInsercao, raio, context);
        context.getFormas().add(formaFinal);
        context.setFormaEmAndamento(null);
        context.selecionarForma(formaFinal, false);
        centroInsercao = null;
        context.requestRedraw();
        context.requestStatsUpdate();
    }

    @Override
    public void aoClicar(MouseEvent event, EditorContext context) {
        // Manipulação completa no ciclo pressionar -> arrastar -> soltar
    }
}
