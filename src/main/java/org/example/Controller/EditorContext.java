package org.example.Controller;

import org.example.Models.FormaGeometrica;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Contexto da aplicação que mantém o estado central do editor, formas ativas,
 * seleção, ferramenta atual e configurações da grade.
 */
public class EditorContext {
    private List<FormaGeometrica> formas;
    private List<FormaGeometrica> selecionados;
    private FormaGeometrica formaEmAndamento;
    private Ferramenta ferramentaAtual;
    private boolean gridSnappingHabilitado;

    private double gridSize;
    private String corPreenchimentoAtual;
    private String corBordaAtual;
    private double espessuraBordaAtual;
    private boolean alteracoesNaoSalvas;

    private final HistoricoController historicoController;
    private Runnable onRedrawCallback;
    private Runnable onStatsUpdateCallback;

    public EditorContext() {
        this.formas = new ArrayList<>();
        this.selecionados = new ArrayList<>();
        this.formaEmAndamento = null;
        this.ferramentaAtual = null;
        this.gridSnappingHabilitado = false;
        this.gridSize = 20.0;
        this.corPreenchimentoAtual = "#80C5DE";
        this.corBordaAtual = "#2C3E50";
        this.espessuraBordaAtual = 2.0;
        this.alteracoesNaoSalvas = false;
        this.historicoController = new HistoricoController();
    }

    public List<FormaGeometrica> getFormas() {
        return formas;
    }

    public void setFormas(List<FormaGeometrica> formas) {
        this.formas = (formas != null) ? formas : new ArrayList<>();
        this.selecionados.clear();
        this.formaEmAndamento = null;
        requestRedraw();
        requestStatsUpdate();
    }

    public List<FormaGeometrica> getSelecionados() {
        return selecionados;
    }

    public void setSelecionados(List<FormaGeometrica> selecionados) {
        this.selecionados = (selecionados != null) ? selecionados : new ArrayList<>();
        requestRedraw();
        requestStatsUpdate();
    }

    public FormaGeometrica getFormaEmAndamento() {
        return formaEmAndamento;
    }

    public void setFormaEmAndamento(FormaGeometrica formaEmAndamento) {
        this.formaEmAndamento = formaEmAndamento;
        requestRedraw();
    }

    public Ferramenta getFerramentaAtual() {
        return ferramentaAtual;
    }

    public void setFerramentaAtual(Ferramenta ferramentaAtual) {
        this.ferramentaAtual = ferramentaAtual;
        this.formaEmAndamento = null;
        requestRedraw();
    }

    public boolean isGridSnappingHabilitado() {
        return gridSnappingHabilitado;
    }

    public void setGridSnappingHabilitado(boolean gridSnappingHabilitado) {
        this.gridSnappingHabilitado = gridSnappingHabilitado;
        requestRedraw();
    }

    public double getGridSize() {
        return gridSize;
    }

    public void setGridSize(double gridSize) {
        this.gridSize = Math.max(5.0, gridSize);
        requestRedraw();
    }

    public String getCorPreenchimentoAtual() {
        return corPreenchimentoAtual;
    }

    public void setCorPreenchimentoAtual(String corPreenchimentoAtual) {
        this.corPreenchimentoAtual = corPreenchimentoAtual;
        // Atualiza a cor de preenchimento das formas selecionadas se houver
        if (!selecionados.isEmpty()) {
            registrarHistorico();
            for (FormaGeometrica f : selecionados) {
                f.setCorPreenchimentoHex(corPreenchimentoAtual);
            }
            requestRedraw();
        }
    }

    public String getCorBordaAtual() {
        return corBordaAtual;
    }

    public void setCorBordaAtual(String corBordaAtual) {
        this.corBordaAtual = corBordaAtual;
        if (!selecionados.isEmpty()) {
            registrarHistorico();
            for (FormaGeometrica f : selecionados) {
                f.setCorBordaHex(corBordaAtual);
            }
            requestRedraw();
        }
    }

    public void setCorPreenchimentoHex(String hex) {
        setCorPreenchimentoAtual(hex);
    }

    public void setCorBordaHex(String hex) {
        setCorBordaAtual(hex);
    }

    public double getEspessuraBordaAtual() {
        return espessuraBordaAtual;
    }

    public void setEspessuraBordaAtual(double espessuraBordaAtual) {
        this.espessuraBordaAtual = espessuraBordaAtual;
        if (!selecionados.isEmpty()) {
            registrarHistorico();
            for (FormaGeometrica f : selecionados) {
                f.setEspessuraBorda(espessuraBordaAtual);
            }
            requestRedraw();
        }
    }

    public boolean isAlteracoesNaoSalvas() {
        return alteracoesNaoSalvas;
    }

    public void setAlteracoesNaoSalvas(boolean alteracoesNaoSalvas) {
        this.alteracoesNaoSalvas = alteracoesNaoSalvas;
    }

    public HistoricoController getHistoricoController() {
        return historicoController;
    }

    public void setOnRedrawCallback(Runnable onRedrawCallback) {
        this.onRedrawCallback = onRedrawCallback;
    }

    public void setOnStatsUpdateCallback(Runnable onStatsUpdateCallback) {
        this.onStatsUpdateCallback = onStatsUpdateCallback;
    }

    /**
     * Notifica a view para redesenhar a área gráfica.
     */
    public void requestRedraw() {
        if (onRedrawCallback != null) {
            onRedrawCallback.run();
        }
    }

    /**
     * Notifica a view para atualizar a barra de status.
     */
    public void requestStatsUpdate() {
        if (onStatsUpdateCallback != null) {
            onStatsUpdateCallback.run();
        }
    }

    /**
     * Salva o estado atual na pilha de histórico.
     */
    public void registrarHistorico() {
        historicoController.registrarEstado(this.formas);
        this.alteracoesNaoSalvas = true;
    }

    public void desfazer() {
        if (historicoController.podeDesfazer()) {
            this.formas = historicoController.desfazer(this.formas);
            this.selecionados.clear();
            this.formaEmAndamento = null;
            this.alteracoesNaoSalvas = true;
            requestRedraw();
            requestStatsUpdate();
        }
    }

    public void refazer() {
        if (historicoController.podeRefazer()) {
            this.formas = historicoController.refazer(this.formas);
            this.selecionados.clear();
            this.formaEmAndamento = null;
            this.alteracoesNaoSalvas = true;
            requestRedraw();
            requestStatsUpdate();
        }
    }

    /**
     * Aplica alinhamento magnético com a grade se o snap estiver habilitado.
     */
    public double aplicarSnap(double valor) {
        if (gridSnappingHabilitado && gridSize > 0) {
            return Math.round(valor / gridSize) * gridSize;
        }
        return valor;
    }

    /**
     * Seleciona uma forma, garantindo que outras do mesmo grupo (Composite) sejam selecionadas juntas.
     */
    public void selecionarForma(FormaGeometrica forma, boolean acumular) {
        if (!acumular) {
            selecionados.clear();
        }

        if (forma != null) {
            String gid = forma.getGroupId();
            if (gid != null && !gid.isEmpty()) {
                // Seleciona todas as formas com o mesmo groupId (Padrão Composite)
                for (FormaGeometrica f : formas) {
                    if (gid.equals(f.getGroupId()) && !selecionados.contains(f)) {
                        selecionados.add(f);
                    }
                }
            } else {
                if (!selecionados.contains(forma)) {
                    selecionados.add(forma);
                }
            }
        }
        requestRedraw();
        requestStatsUpdate();
    }

    public void limparSelecao() {
        selecionados.clear();
        requestRedraw();
        requestStatsUpdate();
    }

    /**
     * Agrupa as formas selecionadas sob um mesmo identificador groupId (Padrão Composite).
     */
    public void agruparSelecionados() {
        if (selecionados.size() >= 2) {
            registrarHistorico();
            String novoGroupId = "grupo-" + UUID.randomUUID().toString().substring(0, 8);
            for (FormaGeometrica f : selecionados) {
                f.setGroupId(novoGroupId);
            }
            requestRedraw();
            requestStatsUpdate();
        }
    }

    /**
     * Remove o identificador de grupo das formas selecionadas.
     */
    public void desagruparSelecionados() {
        if (!selecionados.isEmpty()) {
            registrarHistorico();
            for (FormaGeometrica f : selecionados) {
                f.setGroupId(null);
            }
            requestRedraw();
            requestStatsUpdate();
        }
    }

    /**
     * Traz as formas selecionadas para o topo da pilha visual de camadas (frente).
     */
    public void trazerParaFrente() {
        if (!selecionados.isEmpty()) {
            registrarHistorico();
            formas.removeAll(selecionados);
            formas.addAll(selecionados);
            requestRedraw();
        }
    }

    /**
     * Envia as formas selecionadas para o fundo da pilha visual de camadas (trás).
     */
    public void enviarParaTras() {
        if (!selecionados.isEmpty()) {
            registrarHistorico();
            formas.removeAll(selecionados);
            formas.addAll(0, selecionados);
            requestRedraw();
        }
    }

    /**
     * Exclui as formas selecionadas da cena.
     */
    public void excluirSelecionados() {
        if (!selecionados.isEmpty()) {
            registrarHistorico();
            formas.removeAll(selecionados);
            selecionados.clear();
            requestRedraw();
            requestStatsUpdate();
        }
    }
}
