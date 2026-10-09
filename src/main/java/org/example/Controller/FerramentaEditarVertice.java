package org.example.Controller;

import javafx.scene.input.MouseEvent;
import org.example.Models.FormaGeometrica;
import org.example.Models.Ponto;

import java.util.List;

/**
 * Ferramenta interativa para ajustar a posição de vértices individuais de uma forma geométrica.
 */
public class FerramentaEditarVertice implements Ferramenta {
    private Ponto verticeSelecionado;
    private FormaGeometrica formaAssociada;
    private boolean historicoGravado;

    public FerramentaEditarVertice() {
        this.verticeSelecionado = null;
        this.formaAssociada = null;
        this.historicoGravado = false;
    }

    public Ponto getVerticeSelecionado() {
        return verticeSelecionado;
    }

    @Override
    public void aoPressionarMouse(MouseEvent event, EditorContext context) {
        double mx = event.getX();
        double my = event.getY();
        verticeSelecionado = null;
        formaAssociada = null;
        historicoGravado = false;

        // Procura primeiro nas formas já selecionadas
        List<FormaGeometrica> candidatas = !context.getSelecionados().isEmpty()
                ? context.getSelecionados()
                : context.getFormas();

        for (FormaGeometrica forma : candidatas) {
            for (Ponto p : forma.getPontos()) {
                if (p.distancia(new Ponto(mx, my)) <= 10.0) {
                    verticeSelecionado = p;
                    formaAssociada = forma;
                    context.selecionarForma(forma, false);
                    return;
                }
            }
        }
    }

    @Override
    public void aoArrastarMouse(MouseEvent event, EditorContext context) {
        if (verticeSelecionado != null) {
            if (!historicoGravado) {
                context.registrarHistorico();
                historicoGravado = true;
            }

            double x = context.aplicarSnap(event.getX());
            double y = context.aplicarSnap(event.getY());
            verticeSelecionado.setX(x);
            verticeSelecionado.setY(y);

            context.requestRedraw();
            context.requestStatsUpdate();
        }
    }

    @Override
    public void aoSoltarMouse(MouseEvent event, EditorContext context) {
        verticeSelecionado = null;
        formaAssociada = null;
        historicoGravado = false;
        context.requestRedraw();
        context.requestStatsUpdate();
    }

    @Override
    public void aoClicar(MouseEvent event, EditorContext context) {
        // Manipulação primária via arrasto de vértices
    }
}
