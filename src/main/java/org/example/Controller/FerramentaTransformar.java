package org.example.Controller;

import javafx.scene.input.MouseEvent;
import org.example.Models.FormaGeometrica;
import org.example.Models.Ponto;

import java.util.List;

/**
 * Ferramenta que aplica transformações geométricas (Translação, Escala, Rotação e Cisalhamento)
 * nas formas selecionadas, respeitando o agrupamento (Composite).
 */
public class FerramentaTransformar implements Ferramenta {
    private ModoFerramenta modo;

    private double mouseUltimoX;
    private double mouseUltimoY;
    private boolean transformacaoIniciada;
    private Ponto centroideComum;

    public FerramentaTransformar() {
        this(ModoFerramenta.TRANSLADAR);
    }

    public FerramentaTransformar(ModoFerramenta modo) {
        this.modo = (modo != null) ? modo : ModoFerramenta.TRANSLADAR;
        this.transformacaoIniciada = false;
    }

    public ModoFerramenta getModo() {
        return modo;
    }

    public void setModo(ModoFerramenta modo) {
        this.modo = modo;
    }

    private Ponto calcularCentroideComum(List<FormaGeometrica> selecionados) {
        if (selecionados.isEmpty()) {
            return new Ponto(0, 0);
        }
        double sx = 0, sy = 0;
        for (FormaGeometrica f : selecionados) {
            Ponto c = f.getCentroide();
            sx += c.getX();
            sy += c.getY();
        }
        return new Ponto(sx / selecionados.size(), sy / selecionados.size());
    }

    @Override
    public void aoPressionarMouse(MouseEvent event, EditorContext context) {
        mouseUltimoX = event.getX();
        mouseUltimoY = event.getY();
        transformacaoIniciada = false;

        // Se nada estiver selecionado, seleciona a forma sob o cursor
        if (context.getSelecionados().isEmpty()) {
            List<FormaGeometrica> formas = context.getFormas();
            for (int i = formas.size() - 1; i >= 0; i--) {
                FormaGeometrica f = formas.get(i);
                if (f.contemPonto(event.getX(), event.getY())) {
                    context.selecionarForma(f, false);
                    break;
                }
            }
        }

        centroideComum = calcularCentroideComum(context.getSelecionados());
    }

    @Override
    public void aoArrastarMouse(MouseEvent event, EditorContext context) {
        List<FormaGeometrica> selecionados = context.getSelecionados();
        if (selecionados.isEmpty()) {
            return;
        }

        double curX = event.getX();
        double curY = event.getY();

        if (!transformacaoIniciada) {
            context.registrarHistorico();
            transformacaoIniciada = true;
        }

        switch (modo) {
            case TRANSLADAR: {
                double dx = curX - mouseUltimoX;
                double dy = curY - mouseUltimoY;
                for (FormaGeometrica f : selecionados) {
                    f.transladar(dx, dy);
                }
                break;
            }
            case ESCALAR: {
                double distAnt = centroideComum.distancia(new Ponto(mouseUltimoX, mouseUltimoY));
                double distCur = centroideComum.distancia(new Ponto(curX, curY));
                if (distAnt > 5.0 && distCur > 5.0) {
                    double fator = distCur / distAnt;
                    for (FormaGeometrica f : selecionados) {
                        f.escalar(fator);
                    }
                }
                break;
            }
            case ROTACIONAR: {
                double angAnt = Math.atan2(mouseUltimoY - centroideComum.getY(), mouseUltimoX - centroideComum.getX());
                double angCur = Math.atan2(curY - centroideComum.getY(), curX - centroideComum.getX());
                double deltaAng = angCur - angAnt;
                for (FormaGeometrica f : selecionados) {
                    f.rotacionar(deltaAng);
                }
                break;
            }
            case CISALHAR: {
                double shx = (curX - mouseUltimoX) / 100.0;
                double shy = (curY - mouseUltimoY) / 100.0;
                for (FormaGeometrica f : selecionados) {
                    f.cisalhar(shx, shy);
                }
                break;
            }
        }

        mouseUltimoX = curX;
        mouseUltimoY = curY;
        centroideComum = calcularCentroideComum(selecionados);
        context.requestRedraw();
        context.requestStatsUpdate();
    }

    @Override
    public void aoSoltarMouse(MouseEvent event, EditorContext context) {
        transformacaoIniciada = false;
        context.requestRedraw();
        context.requestStatsUpdate();
    }

    @Override
    public void aoClicar(MouseEvent event, EditorContext context) {
        // Interações ocorrem via arrasto
    }
}
