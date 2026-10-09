package org.example.Views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import org.example.Controller.EditorContext;
import org.example.Models.FormaGeometrica;

import java.util.List;

/**
 * Painel inferior que exibe métricas em tempo real sobre coordenadas do cursor,
 * quantidade de formas, área e perímetro da seleção.
 */
public class PainelStatusView extends HBox {
    private final EditorContext contexto;

    private final Label lblCoordenadas;
    private final Label lblTotalFormas;
    private final Label lblSelecionadas;
    private final Label lblArea;
    private final Label lblPerimetro;

    private double mouseX;
    private double mouseY;

    public PainelStatusView(EditorContext contexto) {
        this.contexto = contexto;
        this.mouseX = 0.0;
        this.mouseY = 0.0;

        setAlignment(Pos.CENTER_LEFT);
        setPadding(new Insets(6, 16, 6, 16));
        setSpacing(14);
        setStyle("-fx-background-color: #ECEFF1; -fx-border-color: #CFD8DC; -fx-border-width: 1 0 0 0; -fx-font-family: 'Segoe UI', sans-serif; -fx-font-size: 11px;");

        lblCoordenadas = new Label("X: 0.0 | Y: 0.0");
        lblCoordenadas.setStyle("-fx-font-weight: bold; -fx-text-fill: #37474F;");

        lblTotalFormas = new Label("Formas: 0");
        lblTotalFormas.setStyle("-fx-text-fill: #455A64;");

        lblSelecionadas = new Label("Selecionadas: 0");
        lblSelecionadas.setStyle("-fx-text-fill: #1976D2; -fx-font-weight: bold;");

        lblArea = new Label("Área: 0.0 px²");
        lblArea.setStyle("-fx-text-fill: #2E7D32; -fx-font-weight: bold;");

        lblPerimetro = new Label("Perímetro: 0.0 px");
        lblPerimetro.setStyle("-fx-text-fill: #C2185B; -fx-font-weight: bold;");

        getChildren().addAll(
                lblCoordenadas,
                new Separator(javafx.geometry.Orientation.VERTICAL),
                lblTotalFormas,
                new Separator(javafx.geometry.Orientation.VERTICAL),
                lblSelecionadas,
                new Separator(javafx.geometry.Orientation.VERTICAL),
                lblArea,
                new Separator(javafx.geometry.Orientation.VERTICAL),
                lblPerimetro
        );

        atualizar();
    }

    public void setCoordenadasMouse(double x, double y) {
        this.mouseX = x;
        this.mouseY = y;
        lblCoordenadas.setText(String.format("X: %.1f | Y: %.1f", mouseX, mouseY));
    }

    /**
     * Atualiza as informações exibidas no painel de status com base no estado do EditorContext.
     */
    public void atualizar() {
        if (contexto == null) return;

        int total = contexto.getFormas().size();
        List<FormaGeometrica> selecionados = contexto.getSelecionados();
        int qtdSelecionados = selecionados.size();

        lblTotalFormas.setText("Formas: " + total);
        lblSelecionadas.setText("Selecionadas: " + qtdSelecionados);

        double areaTotal = 0.0;
        double perimetroTotal = 0.0;

        List<FormaGeometrica> formasParaCalculo = qtdSelecionados > 0 ? selecionados : contexto.getFormas();
        for (FormaGeometrica f : formasParaCalculo) {
            areaTotal += f.calcularArea();
            perimetroTotal += f.calcularPerimetro();
        }

        String prefixo = qtdSelecionados > 0 ? "Área (Sel): " : "Área Total: ";
        String prefixoPerim = qtdSelecionados > 0 ? "Perímetro (Sel): " : "Perímetro Total: ";

        lblArea.setText(String.format("%s%.1f px²", prefixo, areaTotal));
        lblPerimetro.setText(String.format("%s%.1f px", prefixoPerim, perimetroTotal));
    }
}
