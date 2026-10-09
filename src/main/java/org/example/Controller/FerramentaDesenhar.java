package org.example.Controller;

import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.example.Models.Ponto;
import org.example.Models.Poligono;

/**
 * Ferramenta interativa para desenhar polígonos ponto a ponto através de cliques.
 */
public class FerramentaDesenhar implements Ferramenta {

    @Override
    public void aoPressionarMouse(MouseEvent event, EditorContext context) {
        // Manipulação primária feita no aoClicar
    }

    @Override
    public void aoArrastarMouse(MouseEvent event, EditorContext context) {
        // Prévia dinâmica se necessário
    }

    @Override
    public void aoSoltarMouse(MouseEvent event, EditorContext context) {
        // Ações finalizadas no aoClicar
    }

    @Override
    public void aoClicar(MouseEvent event, EditorContext context) {
        double x = context.aplicarSnap(event.getX());
        double y = context.aplicarSnap(event.getY());
        Ponto novoPonto = new Ponto(x, y);

        // Botão secundário ou clique duplo fecha o polígono
        if (event.getButton() == MouseButton.SECONDARY || event.getClickCount() == 2) {
            finalizarPoligono(context);
            return;
        }

        if (event.getButton() == MouseButton.PRIMARY) {
            if (context.getFormaEmAndamento() == null) {
                // Inicia um novo polígono
                Poligono novoPoligono = new Poligono();
                novoPoligono.setCorPreenchimentoHex(context.getCorPreenchimentoAtual());
                novoPoligono.setCorBordaHex(context.getCorBordaAtual());
                novoPoligono.setEspessuraBorda(context.getEspessuraBordaAtual());
                novoPoligono.adicionarPonto(novoPonto);
                context.setFormaEmAndamento(novoPoligono);
            } else if (context.getFormaEmAndamento() instanceof Poligono) {
                Poligono poli = (Poligono) context.getFormaEmAndamento();
                if (!poli.getPontos().isEmpty()) {
                    Ponto primeiro = poli.getPontos().get(0);
                    // Se clicar próximo ao primeiro vértice (tolerância de 15px) e tiver pelo menos 3 pontos
                    if (primeiro.distancia(novoPonto) < 15.0 && poli.getQuantidadePontos() >= 3) {
                        finalizarPoligono(context);
                        return;
                    }
                }
                poli.adicionarPonto(novoPonto);
                context.requestRedraw();
                context.requestStatsUpdate();
            }
        }
    }

    private void finalizarPoligono(EditorContext context) {
        if (context.getFormaEmAndamento() instanceof Poligono) {
            Poligono poli = (Poligono) context.getFormaEmAndamento();
            if (poli.isValido()) {
                context.registrarHistorico();
                context.getFormas().add(poli);
                context.setFormaEmAndamento(null);
                context.selecionarForma(poli, false);
            } else {
                // Polígono inválido com menos de 3 pontos é descartado
                context.setFormaEmAndamento(null);
                context.requestRedraw();
            }
        }
    }
}
