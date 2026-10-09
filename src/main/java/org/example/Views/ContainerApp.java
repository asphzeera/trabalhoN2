package org.example.Views;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.Controller.*;
import org.example.Models.*;

import java.io.File;
import java.util.List;
import java.util.Optional;

/**
 * Contêiner principal da aplicação que integra a área de desenho (Canvas),
 * a barra lateral de ferramentas e o painel de status inferior.
 */
public class ContainerApp extends BorderPane {
    private Canvas areaDeDesenho;
    private EditorContext contexto;
    private BarraLateralView barraLateral;
    private PainelStatusView painelStatus;
    private final PersistenciaService persistenciaService;
    private Stage stagePrincipal;

    public ContainerApp() {
        this(null);
    }

    public ContainerApp(Stage stage) {
        this.stagePrincipal = stage;
        this.contexto = new EditorContext();
        this.persistenciaService = new PersistenciaService();

        // Configuração do Canvas
        this.areaDeDesenho = new Canvas(1400, 900);

        // Subviews
        this.barraLateral = new BarraLateralView(this.contexto);
        this.painelStatus = new PainelStatusView(this.contexto);

        // Ações da barra lateral para salvar e carregar
        this.barraLateral.setOnSalvarAcao(v -> salvarArquivo());
        this.barraLateral.setOnCarregarAcao(v -> carregarArquivo());

        // Conectar callbacks do EditorContext com as Views
        this.contexto.setOnRedrawCallback(this::redesenharCanvas);
        this.contexto.setOnStatsUpdateCallback(() -> {
            this.painelStatus.atualizar();
            this.barraLateral.atualizarCoresInterface();
        });

        // Painel central contendo a área de desenho
        Pane canvasHolder = new Pane(areaDeDesenho);
        canvasHolder.setStyle("-fx-background-color: #FFFFFF;");
        canvasHolder.widthProperty().addListener((obs, oldVal, newVal) -> {
            areaDeDesenho.setWidth(Math.max(1400, newVal.doubleValue()));
            redesenharCanvas();
        });
        canvasHolder.heightProperty().addListener((obs, oldVal, newVal) -> {
            areaDeDesenho.setHeight(Math.max(900, newVal.doubleValue()));
            redesenharCanvas();
        });

        this.setLeft(this.barraLateral);
        this.setCenter(canvasHolder);
        this.setBottom(this.painelStatus);

        inicializarEventos();
        redesenharCanvas();
    }

    public void setStagePrincipal(Stage stage) {
        this.stagePrincipal = stage;
        if (stagePrincipal != null) {
            configurarAlertaFechamento();
        }
    }

    /**
     * Configura a verificação de alterações não salvas antes de fechar a janela.
     */
    private void configurarAlertaFechamento() {
        stagePrincipal.setOnCloseRequest(event -> {
            if (contexto.isAlteracoesNaoSalvas()) {
                Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
                confirmacao.setTitle("Alterações não salvas");
                confirmacao.setHeaderText("Você possui alterações não salvas no projeto.");
                confirmacao.setContentText("Deseja realmente sair sem salvar?");

                ButtonType btnSalvar = new ButtonType("Salvar Agora");
                ButtonType btnSairSemSalvar = new ButtonType("Sair sem Salvar");
                ButtonType btnCancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);

                confirmacao.getButtonTypes().setAll(btnSalvar, btnSairSemSalvar, btnCancelar);

                Optional<ButtonType> resultado = confirmacao.showAndWait();
                if (resultado.isPresent()) {
                    if (resultado.get() == btnSalvar) {
                        salvarArquivo();
                        if (contexto.isAlteracoesNaoSalvas()) {
                            // Se o usuário cancelou o diálogo de salvar
                            event.consume();
                        }
                    } else if (resultado.get() == btnCancelar) {
                        event.consume();
                    }
                }
            }
        });
    }

    /**
     * Inicializa os ouvintes de eventos de mouse no canvas e teclado global.
     */
    public void inicializarEventos() {
        areaDeDesenho.setOnMousePressed(e -> {
            if (contexto.getFerramentaAtual() != null) {
                contexto.getFerramentaAtual().aoPressionarMouse(e, contexto);
            }
            painelStatus.setCoordenadasMouse(e.getX(), e.getY());
        });

        areaDeDesenho.setOnMouseDragged(e -> {
            if (contexto.getFerramentaAtual() != null) {
                contexto.getFerramentaAtual().aoArrastarMouse(e, contexto);
            }
            painelStatus.setCoordenadasMouse(e.getX(), e.getY());
        });

        areaDeDesenho.setOnMouseReleased(e -> {
            if (contexto.getFerramentaAtual() != null) {
                contexto.getFerramentaAtual().aoSoltarMouse(e, contexto);
            }
            painelStatus.setCoordenadasMouse(e.getX(), e.getY());
        });

        areaDeDesenho.setOnMouseClicked(e -> {
            if (contexto.getFerramentaAtual() != null) {
                contexto.getFerramentaAtual().aoClicar(e, contexto);
            }
        });

        areaDeDesenho.setOnMouseMoved(e -> {
            painelStatus.setCoordenadasMouse(e.getX(), e.getY());
        });

        // Atalhos de teclado
        this.addEventFilter(KeyEvent.KEY_PRESSED, this::processarAtalhosTeclado);
    }

    private void processarAtalhosTeclado(KeyEvent event) {
        if (event.isControlDown()) {
            if (event.getCode() == KeyCode.Z) {
                contexto.desfazer();
                event.consume();
            } else if (event.getCode() == KeyCode.Y) {
                contexto.refazer();
                event.consume();
            } else if (event.getCode() == KeyCode.S) {
                salvarArquivo();
                event.consume();
            } else if (event.getCode() == KeyCode.O) {
                carregarArquivo();
                event.consume();
            } else if (event.getCode() == KeyCode.G) {
                contexto.agruparSelecionados();
                event.consume();
            } else if (event.getCode() == KeyCode.U) {
                contexto.desagruparSelecionados();
                event.consume();
            }
        } else if (event.getCode() == KeyCode.DELETE || event.getCode() == KeyCode.BACK_SPACE) {
            contexto.excluirSelecionados();
            event.consume();
        }
    }

    /**
     * Limpa e redesenha a grade, formas existentes, prévias e destaques de seleção.
     */
    private void redesenharCanvas() {
        GraphicsContext gc = areaDeDesenho.getGraphicsContext2D();
        double w = areaDeDesenho.getWidth();
        double h = areaDeDesenho.getHeight();

        // Limpar fundo
        gc.clearRect(0, 0, w, h);
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, w, h);

        // Grade magnética / de fundo
        desenharGrade(gc, w, h);

        // Desenhar formas adicionadas
        List<FormaGeometrica> selecionados = contexto.getSelecionados();
        for (FormaGeometrica forma : contexto.getFormas()) {
            boolean estaSelecionada = selecionados.contains(forma);
            desenharForma(forma, estaSelecionada);
        }

        // Desenhar forma em andamento (criação dinâmica)
        FormaGeometrica emAndamento = contexto.getFormaEmAndamento();
        if (emAndamento != null) {
            desenharForma(emAndamento, false);
            // Desenhar linhas guia para polígono em construção
            if (emAndamento instanceof Poligono && emAndamento.getQuantidadePontos() > 0) {
                gc.setStroke(Color.rgb(30, 136, 229, 0.7));
                gc.setLineWidth(1.5);
                gc.setLineDashes(4.0);
                List<Ponto> pts = emAndamento.getPontos();
                for (int i = 0; i < pts.size() - 1; i++) {
                    gc.strokeLine(pts.get(i).getX(), pts.get(i).getY(), pts.get(i + 1).getX(), pts.get(i + 1).getY());
                }
                gc.setLineDashes((double[]) null);
            }
        }

        // Desenhar retângulo elástico de seleção se a ferramenta de seleção estiver ativa
        if (contexto.getFerramentaAtual() instanceof FerramentaSelecionar) {
            FerramentaSelecionar fSel = (FerramentaSelecionar) contexto.getFerramentaAtual();
            if (fSel.isArrastandoCaixa()) {
                double rx = fSel.getSelecaoMinX();
                double ry = fSel.getSelecaoMinY();
                double rw = fSel.getSelecaoLargura();
                double rh = fSel.getSelecaoAltura();

                gc.setFill(Color.rgb(33, 150, 243, 0.18));
                gc.fillRect(rx, ry, rw, rh);
                gc.setStroke(Color.rgb(33, 150, 243, 0.85));
                gc.setLineWidth(1.0);
                gc.setLineDashes(5.0);
                gc.strokeRect(rx, ry, rw, rh);
                gc.setLineDashes((double[]) null);
            }
        }
    }

    private void desenharGrade(GraphicsContext gc, double w, double h) {
        double step = contexto.getGridSize();
        gc.setFill(Color.rgb(220, 224, 230));
        for (double x = 0; x < w; x += step) {
            for (double y = 0; y < h; y += step) {
                gc.fillOval(x - 1, y - 1, 2, 2);
            }
        }
    }

    /**
     * Renderiza uma forma geométrica no canvas, aplicando contorno, preenchimento e
     * destaque visual caso esteja selecionada.
     */
    private void desenharForma(FormaGeometrica forma, boolean selecionada) {
        if (forma == null) return;

        GraphicsContext gc = areaDeDesenho.getGraphicsContext2D();

        Color fillCor;
        try {
            fillCor = Color.web(forma.getCorPreenchimentoHex());
        } catch (Exception e) {
            fillCor = Color.LIGHTBLUE;
        }

        Color bordaCor;
        try {
            bordaCor = Color.web(forma.getCorBordaHex());
        } catch (Exception e) {
            bordaCor = Color.DARKSLATEGRAY;
        }

        gc.setFill(fillCor);
        gc.setStroke(bordaCor);
        gc.setLineWidth(forma.getEspessuraBorda());
        gc.setLineDashes((double[]) null);

        if (forma instanceof Circulo) {
            Circulo c = (Circulo) forma;
            double r = c.getRaio();
            double cx = c.getCentro().getX();
            double cy = c.getCentro().getY();

            gc.fillOval(cx - r, cy - r, r * 2.0, r * 2.0);
            gc.strokeOval(cx - r, cy - r, r * 2.0, r * 2.0);

            if (selecionada) {
                // Destaque visual para o círculo
                gc.setStroke(Color.rgb(30, 136, 229, 0.9));
                gc.setLineWidth(1.5);
                gc.setLineDashes(4.0);
                gc.strokeRect(cx - r - 4, cy - r - 4, (r * 2.0) + 8, (r * 2.0) + 8);
                gc.setLineDashes((double[]) null);

                // Alça de centro
                desenharAlca(gc, cx, cy);
                desenharAlca(gc, cx + r, cy);
            }
        } else {
            List<Ponto> pontos = forma.getPontos();
            int n = pontos.size();
            if (n >= 2) {
                double[] xPoints = new double[n];
                double[] yPoints = new double[n];
                for (int i = 0; i < n; i++) {
                    xPoints[i] = pontos.get(i).getX();
                    yPoints[i] = pontos.get(i).getY();
                }

                if (n >= 3) {
                    gc.fillPolygon(xPoints, yPoints, n);
                    gc.strokePolygon(xPoints, yPoints, n);
                } else {
                    gc.strokePolyline(xPoints, yPoints, n);
                }

                if (selecionada) {
                    // Destaque com alças em cada vértice
                    gc.setStroke(Color.rgb(30, 136, 229, 0.9));
                    gc.setLineWidth(1.5);
                    gc.setLineDashes(4.0);
                    gc.strokePolygon(xPoints, yPoints, n);
                    gc.setLineDashes((double[]) null);

                    for (Ponto p : pontos) {
                        desenharAlca(gc, p.getX(), p.getY());
                    }
                }
            }
        }

        // Se a forma possuir groupId, exibe um sutil indicador visual de agrupamento
        if (forma.getGroupId() != null && selecionada) {
            Ponto c = forma.getCentroide();
            gc.setFill(Color.rgb(76, 175, 80, 0.8));
            gc.fillOval(c.getX() - 4, c.getY() - 4, 8, 8);
        }
    }

    private void desenharAlca(GraphicsContext gc, double x, double y) {
        gc.setFill(Color.WHITE);
        gc.setStroke(Color.rgb(25, 118, 210));
        gc.setLineWidth(1.5);
        gc.fillRect(x - 3.5, y - 3.5, 7, 7);
        gc.strokeRect(x - 3.5, y - 3.5, 7, 7);
    }

    /**
     * Salva o estado atual em arquivo JSON com tratamento de erro e diálogo explicativo.
     */
    private void salvarArquivo() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Salvar Projeto Vetorial");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Arquivo JSON de Desenho (*.json)", "*.json")
        );
        fileChooser.setInitialFileName("desenho.json");

        File arquivo = fileChooser.showSaveDialog(stagePrincipal);
        if (arquivo != null) {
            try {
                persistenciaService.salvarParaArquivo(arquivo, contexto, areaDeDesenho.getWidth(), areaDeDesenho.getHeight());
                contexto.setAlteracoesNaoSalvas(false);

                Alert info = new Alert(Alert.AlertType.INFORMATION);
                info.setTitle("Projeto Salvo");
                info.setHeaderText(null);
                info.setContentText("O desenho vetorial foi salvo com sucesso em:\n" + arquivo.getAbsolutePath());
                info.showAndWait();
            } catch (Exception ex) {
                Alert erro = new Alert(Alert.AlertType.ERROR);
                erro.setTitle("Erro ao Salvar");
                erro.setHeaderText("Não foi possível salvar o arquivo.");
                erro.setContentText("Detalhes do erro: " + ex.getMessage());
                erro.showAndWait();
            }
        }
    }

    /**
     * Carrega projeto de arquivo JSON com tratamento de erros para arquivos corrompidos.
     */
    private void carregarArquivo() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Abrir Projeto Vetorial");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Arquivo JSON de Desenho (*.json)", "*.json")
        );

        File arquivo = fileChooser.showOpenDialog(stagePrincipal);
        if (arquivo != null) {
            try {
                PersistenciaService.ProjetoDTO projeto = persistenciaService.carregarDeArquivo(arquivo);
                List<FormaGeometrica> formasCarregadas = persistenciaService.converterParaFormas(projeto);

                contexto.setFormas(formasCarregadas);
                contexto.setGridSnappingHabilitado(projeto.gridSnapping);
                if (projeto.gridSize > 0) {
                    contexto.setGridSize(projeto.gridSize);
                }
                contexto.getHistoricoController().limpar();
                contexto.registrarHistorico();
                contexto.setAlteracoesNaoSalvas(false);

                Alert info = new Alert(Alert.AlertType.INFORMATION);
                info.setTitle("Projeto Carregado");
                info.setHeaderText(null);
                info.setContentText("Desenho carregado com sucesso! Total de formas: " + formasCarregadas.size());
                info.showAndWait();
            } catch (Exception ex) {
                Alert alerta = new Alert(Alert.AlertType.ERROR);
                alerta.setTitle("Erro ao Carregar Arquivo");
                alerta.setHeaderText("O arquivo selecionado está corrompido, ilegível ou é incompatível.");
                alerta.setContentText("Falha durante o processamento do arquivo:\n" + ex.getMessage()
                        + "\n\nA aplicação permaneceu estável e nenhuma alteração foi perdida.");
                alerta.showAndWait();
            }
        }
    }

    public EditorContext getContexto() {
        return contexto;
    }

    public Canvas getAreaDeDesenho() {
        return areaDeDesenho;
    }
}
