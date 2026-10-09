package org.example.Controller;

import javafx.scene.input.MouseEvent;
import org.example.Models.FormaGeometrica;
import org.example.Models.Ponto;

import java.util.List;

/**
 * Ferramenta responsável pela seleção individual/por caixa de formas
 * e pela translação interativa das formas selecionadas.
 */
public class FerramentaSelecionar implements Ferramenta {
    private boolean arrastandoCaixa;
    private double selecaoStartX;
    private double selecaoStartY;
    private double selecaoAtualX;
    private double selecaoAtualY;

    private double mouseUltimoX;
    private double mouseUltimoY;
    private boolean houveTranslacao;

    public FerramentaSelecionar() {
        this.arrastandoCaixa = false;
        this.houveTranslacao = false;
    }

    public boolean isArrastandoCaixa() {
        return arrastandoCaixa;
    }

    public double getSelecaoMinX() {
        return Math.min(selecaoStartX, selecaoAtualX);
    }

    public double getSelecaoMinY() {
        return Math.min(selecaoStartY, selecaoAtualY);
    }

    public double getSelecaoLargura() {
        return Math.abs(selecaoAtualX - selecaoStartX);
    }

    public double getSelecaoAltura() {
        return Math.abs(selecaoAtualY - selecaoStartY);
    }

    @Override
    public void aoPressionarMouse(MouseEvent event, EditorContext context) {
        double x = event.getX();
        double y = event.getY();
        mouseUltimoX = x;
        mouseUltimoY = y;
        houveTranslacao = false;

        // Localiza forma sob o cursor (ordem de sobreposição: da frente para o fundo)
        FormaGeometrica formaAtingida = null;
        List<FormaGeometrica> formas = context.getFormas();
        for (int i = formas.size() - 1; i >= 0; i--) {
            FormaGeometrica f = formas.get(i);
            if (f.contemPonto(x, y)) {
                formaAtingida = f;
                break;
            }
        }

        if (formaAtingida != null) {
            arrastandoCaixa = false;
            boolean shift = event.isShiftDown() || event.isControlDown();

            if (!context.getSelecionados().contains(formaAtingida)) {
                context.selecionarForma(formaAtingida, shift);
            }
        } else {
            // Clique em área vazia
            if (!event.isShiftDown() && !event.isControlDown()) {
                context.limparSelecao();
            }
            arrastandoCaixa = true;
            selecaoStartX = x;
            selecaoStartY = y;
            selecaoAtualX = x;
            selecaoAtualY = y;
            context.requestRedraw();
        }
    }

    @Override
    public void aoArrastarMouse(MouseEvent event, EditorContext context) {
        double x = event.getX();
        double y = event.getY();

        if (arrastandoCaixa) {
            selecaoAtualX = x;
            selecaoAtualY = y;
            context.requestRedraw();
        } else if (!context.getSelecionados().isEmpty()) {
            double dx = x - mouseUltimoX;
            double dy = y - mouseUltimoY;

            if (!houveTranslacao && (Math.abs(dx) > 1.0 || Math.abs(dy) > 1.0)) {
                context.registrarHistorico();
                houveTranslacao = true;
            }

            if (houveTranslacao) {
                for (FormaGeometrica f : context.getSelecionados()) {
                    f.transladar(dx, dy);
                }
                mouseUltimoX = x;
                mouseUltimoY = y;
                context.requestRedraw();
                context.requestStatsUpdate();
            }
        }
    }

    @Override
    public void aoSoltarMouse(MouseEvent event, EditorContext context) {
        if (arrastandoCaixa) {
            double minX = getSelecaoMinX();
            double maxX = minX + getSelecaoLargura();
            double minY = getSelecaoMinY();
            double maxY = minY + getSelecaoAltura();

            if (getSelecaoLargura() > 3.0 || getSelecaoAltura() > 3.0) {
                for (FormaGeometrica f : context.getFormas()) {
                    Ponto c = f.getCentroide();
                    if (c.getX() >= minX && c.getX() <= maxX && c.getY() >= minY && c.getY() <= maxY) {
                        context.selecionarForma(f, true);
                    }
                }
            }
            arrastandoCaixa = false;
            context.requestRedraw();
            context.requestStatsUpdate();
        }
    }

    @Override
    public void aoClicar(MouseEvent event, EditorContext context) {
        // Seleção pontual gerenciada no aoPressionarMouse
    }
}
