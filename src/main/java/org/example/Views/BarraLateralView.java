package org.example.Views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.example.Controller.*;
import org.example.Models.FormaGeometrica;

import java.util.function.Consumer;

/**
 * Painel lateral que organiza os controles de estilo, ferramentas, inserção de formas,
 * camadas e ações do sistema.
 */
public class BarraLateralView extends ScrollPane {
    private final EditorContext contexto;
    private final VBox conteudo;

    private ColorPicker pickerPreenchimento;
    private ColorPicker pickerBorda;
    private Slider sliderEspessura;
    private CheckBox chkSnap;

    private ToggleGroup grupoFerramentas;
    private ToggleButton btnPoligono;
    private ToggleButton btnSelecionar;
    private ToggleButton btnTransladar;
    private ToggleButton btnEscalar;
    private ToggleButton btnRotacionar;
    private ToggleButton btnCisalhar;
    private ToggleButton btnEditarVertice;
    private ToggleButton btnInserirCirculo;
    private ToggleButton btnInserirQuadrado;
    private ToggleButton btnInserirHexagono;

    private Consumer<Void> onSalvarAcao;
    private Consumer<Void> onCarregarAcao;

    public BarraLateralView(EditorContext contexto) {
        this.contexto = contexto;
        this.conteudo = new VBox(12);

        this.setFitToWidth(true);
        this.setHbarPolicy(ScrollBarPolicy.NEVER);
        this.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
        this.setStyle("-fx-background: #F4F6F8; -fx-background-color: #F4F6F8; -fx-border-color: #CFD8DC; -fx-border-width: 0 1 0 0;");
        this.setPrefWidth(240);

        this.conteudo.setPadding(new Insets(12, 10, 16, 10));
        this.conteudo.setAlignment(Pos.TOP_CENTER);

        construirSecaoEstilos();
        construirSecaoFerramentas();
        construirSecaoCamadas();

        this.setContent(conteudo);

        // Seleciona a ferramenta de seleção por padrão
        if (btnSelecionar != null) {
            btnSelecionar.setSelected(true);
            btnSelecionar.fire();
        }
    }

    public void setOnSalvarAcao(Consumer<Void> onSalvarAcao) {
        this.onSalvarAcao = onSalvarAcao;
    }

    public void setOnCarregarAcao(Consumer<Void> onCarregarAcao) {
        this.onCarregarAcao = onCarregarAcao;
    }

    private VBox criarCard(String titulo) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(10));
        card.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #E0E0E0; -fx-border-radius: 6; -fx-background-radius: 6; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.04), 4, 0, 0, 1);");

        Label lbl = new Label(titulo);
        lbl.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-text-fill: #455A64; -fx-letter-spacing: 0.5px;");
        card.getChildren().add(lbl);
        return card;
    }

    private void construirSecaoEstilos() {
        VBox cardEstilos = criarCard("ESTILOS");

        Label lblPreenchimento = new Label("Cor Preenchimento:");
        lblPreenchimento.setStyle("-fx-font-size: 11px; -fx-text-fill: #607D8B;");
        pickerPreenchimento = new ColorPicker(Color.web(contexto.getCorPreenchimentoAtual()));
        pickerPreenchimento.setMaxWidth(Double.MAX_VALUE);
        pickerPreenchimento.setOnAction(e -> {
            String hex = toHex(pickerPreenchimento.getValue());
            contexto.setCorPreenchimentoAtual(hex);
        });

        Label lblBorda = new Label("Cor Borda:");
        lblBorda.setStyle("-fx-font-size: 11px; -fx-text-fill: #607D8B;");
        pickerBorda = new ColorPicker(Color.web(contexto.getCorBordaAtual()));
        pickerBorda.setMaxWidth(Double.MAX_VALUE);
        pickerBorda.setOnAction(e -> {
            String hex = toHex(pickerBorda.getValue());
            contexto.setCorBordaAtual(hex);
        });

        Label lblEspessura = new Label("Espessura da Borda:");
        lblEspessura.setStyle("-fx-font-size: 11px; -fx-text-fill: #607D8B;");
        sliderEspessura = new Slider(1.0, 15.0, contexto.getEspessuraBordaAtual());
        sliderEspessura.setShowTickLabels(true);
        sliderEspessura.setShowTickMarks(true);
        sliderEspessura.setMajorTickUnit(5.0);
        sliderEspessura.setBlockIncrement(1.0);
        sliderEspessura.valueProperty().addListener((obs, antigo, novo) -> {
            contexto.setEspessuraBordaAtual(novo.doubleValue());
        });

        chkSnap = new CheckBox("Grade Magnética (Snapping)");
        chkSnap.setStyle("-fx-font-size: 11px; -fx-text-fill: #37474F; -fx-font-weight: bold;");
        chkSnap.setSelected(contexto.isGridSnappingHabilitado());
        chkSnap.setOnAction(e -> contexto.setGridSnappingHabilitado(chkSnap.isSelected()));

        cardEstilos.getChildren().addAll(lblPreenchimento, pickerPreenchimento, lblBorda, pickerBorda, lblEspessura, sliderEspessura, chkSnap);
        conteudo.getChildren().add(cardEstilos);
    }

    private void construirSecaoFerramentas() {
        VBox cardFerramentas = criarCard("FERRAMENTAS");
        grupoFerramentas = new ToggleGroup();

        btnPoligono = criarToggleButton("✏ Polígono (Livre)", grupoFerramentas);
        btnSelecionar = criarToggleButton("↖ Selecionar / Mover", grupoFerramentas);
        btnTransladar = criarToggleButton("✥ Transladar", grupoFerramentas);
        btnEscalar = criarToggleButton("⤢ Escalar", grupoFerramentas);
        btnRotacionar = criarToggleButton("↻ Rotacionar", grupoFerramentas);
        btnCisalhar = criarToggleButton("▱ Cisalhar", grupoFerramentas);
        btnEditarVertice = criarToggleButton("⌖ Editar Vértice", grupoFerramentas);

        btnPoligono.setOnAction(e -> contexto.setFerramentaAtual(new FerramentaDesenhar()));
        btnSelecionar.setOnAction(e -> contexto.setFerramentaAtual(new FerramentaSelecionar()));
        btnTransladar.setOnAction(e -> contexto.setFerramentaAtual(new FerramentaTransformar(ModoFerramenta.TRANSLADAR)));
        btnEscalar.setOnAction(e -> contexto.setFerramentaAtual(new FerramentaTransformar(ModoFerramenta.ESCALAR)));
        btnRotacionar.setOnAction(e -> contexto.setFerramentaAtual(new FerramentaTransformar(ModoFerramenta.ROTACIONAR)));
        btnCisalhar.setOnAction(e -> contexto.setFerramentaAtual(new FerramentaTransformar(ModoFerramenta.CISALHAR)));
        btnEditarVertice.setOnAction(e -> contexto.setFerramentaAtual(new FerramentaEditarVertice()));

        Button btnExcluir = new Button("🗑 Excluir Selecionadas");
        btnExcluir.setMaxWidth(Double.MAX_VALUE);
        btnExcluir.setStyle("-fx-background-color: #FFEBEE; -fx-text-fill: #D32F2F; -fx-border-color: #FFCDD2; -fx-border-radius: 4; -fx-background-radius: 4; -fx-font-size: 11px;");
        btnExcluir.setOnAction(e -> contexto.excluirSelecionados());

        cardFerramentas.getChildren().addAll(
                btnPoligono, btnSelecionar, btnTransladar, btnEscalar,
                btnRotacionar, btnCisalhar, btnEditarVertice, btnExcluir
        );
        conteudo.getChildren().add(cardFerramentas);

        // Seção Formas Prontas
        VBox cardFormas = criarCard("FORMAS");
        btnInserirQuadrado = criarToggleButton("⬛ Inserir Quadrado", grupoFerramentas);
        btnInserirCirculo = criarToggleButton("⬤ Inserir Círculo", grupoFerramentas);
        btnInserirHexagono = criarToggleButton("⬡ Inserir Hexágono", grupoFerramentas);

        btnInserirQuadrado.setOnAction(e -> contexto.setFerramentaAtual(new FerramentaInserirForma(FerramentaInserirForma.TipoForma.QUADRADO)));
        btnInserirCirculo.setOnAction(e -> contexto.setFerramentaAtual(new FerramentaInserirForma(FerramentaInserirForma.TipoForma.CIRCULO)));
        btnInserirHexagono.setOnAction(e -> contexto.setFerramentaAtual(new FerramentaInserirForma(FerramentaInserirForma.TipoForma.HEXAGONO)));

        cardFormas.getChildren().addAll(btnInserirQuadrado, btnInserirCirculo, btnInserirHexagono);
        conteudo.getChildren().add(cardFormas);
    }

    private void construirSecaoCamadas() {
        VBox cardCamadas = criarCard("CAMADAS / GRUPO");

        Button btnFrente = new Button("▲ Trazer p/ Frente");
        btnFrente.setMaxWidth(Double.MAX_VALUE);
        btnFrente.setStyle("-fx-font-size: 11px;");
        btnFrente.setOnAction(e -> contexto.trazerParaFrente());

        Button btnTras = new Button("▼ Enviar p/ Trás");
        btnTras.setMaxWidth(Double.MAX_VALUE);
        btnTras.setStyle("-fx-font-size: 11px;");
        btnTras.setOnAction(e -> contexto.enviarParaTras());

        Button btnAgrupar = new Button("🔗 Agrupar");
        btnAgrupar.setMaxWidth(Double.MAX_VALUE);
        btnAgrupar.setStyle("-fx-font-size: 11px;");
        btnAgrupar.setOnAction(e -> contexto.agruparSelecionados());

        Button btnDesagrupar = new Button("⛓ Desagrupar");
        btnDesagrupar.setMaxWidth(Double.MAX_VALUE);
        btnDesagrupar.setStyle("-fx-font-size: 11px;");
        btnDesagrupar.setOnAction(e -> contexto.desagruparSelecionados());

        cardCamadas.getChildren().addAll(btnFrente, btnTras, btnAgrupar, btnDesagrupar);
        conteudo.getChildren().add(cardCamadas);

        // Seção Ações / Sistema
        VBox cardSistema = criarCard("SISTEMA");

        Button btnDesfazer = new Button("↩ Desfazer (Ctrl+Z)");
        btnDesfazer.setMaxWidth(Double.MAX_VALUE);
        btnDesfazer.setStyle("-fx-font-size: 11px;");
        btnDesfazer.setOnAction(e -> contexto.desfazer());

        Button btnRefazer = new Button("↪ Refazer (Ctrl+Y)");
        btnRefazer.setMaxWidth(Double.MAX_VALUE);
        btnRefazer.setStyle("-fx-font-size: 11px;");
        btnRefazer.setOnAction(e -> contexto.refazer());

        Button btnSalvar = new Button("💾 Salvar");
        btnSalvar.setMaxWidth(Double.MAX_VALUE);
        btnSalvar.setStyle("-fx-font-size: 11px; -fx-background-color: #E8F5E9; -fx-text-fill: #2E7D32; -fx-border-color: #C8E6C9; -fx-border-radius: 4; -fx-background-radius: 4;");
        btnSalvar.setOnAction(e -> {
            if (onSalvarAcao != null) onSalvarAcao.accept(null);
        });

        Button btnCarregar = new Button("📂 Carregar");
        btnCarregar.setMaxWidth(Double.MAX_VALUE);
        btnCarregar.setStyle("-fx-font-size: 11px; -fx-background-color: #E3F2FD; -fx-text-fill: #1565C0; -fx-border-color: #BBDEFB; -fx-border-radius: 4; -fx-background-radius: 4;");
        btnCarregar.setOnAction(e -> {
            if (onCarregarAcao != null) onCarregarAcao.accept(null);
        });

        cardSistema.getChildren().addAll(btnDesfazer, btnRefazer, btnSalvar, btnCarregar);
        conteudo.getChildren().add(cardSistema);
    }

    private ToggleButton criarToggleButton(String texto, ToggleGroup grupo) {
        ToggleButton btn = new ToggleButton(texto);
        btn.setToggleGroup(grupo);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setStyle("-fx-font-size: 11px; -fx-padding: 6 10 6 10;");
        return btn;
    }

    /**
     * Sincroniza os controles visuais de cores e espessura com os atributos das formas selecionadas.
     */
    public void atualizarCoresInterface() {
        if (!contexto.getSelecionados().isEmpty()) {
            FormaGeometrica primeira = contexto.getSelecionados().get(0);
            try {
                pickerPreenchimento.setValue(Color.web(primeira.getCorPreenchimentoHex()));
                pickerBorda.setValue(Color.web(primeira.getCorBordaHex()));
                sliderEspessura.setValue(primeira.getEspessuraBorda());
            } catch (Exception ignored) {}
        }
        chkSnap.setSelected(contexto.isGridSnappingHabilitado());
    }

    private String toHex(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }
}
