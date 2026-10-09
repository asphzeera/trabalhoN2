package org.example.Controller;

import org.example.Models.FormaGeometrica;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Controlador de histórico que implementa os padrões Command e Memento
 * para operações de Desfazer (Undo) e Refazer (Redo).
 */
public class HistoricoController {
    private final Stack<List<FormaGeometrica>> undoStack;
    private final Stack<List<FormaGeometrica>> redoStack;

    public HistoricoController() {
        this.undoStack = new Stack<>();
        this.redoStack = new Stack<>();
    }

    /**
     * Clona profundamente a lista de formas geométricas para isolar completamente o estado.
     */
    private List<FormaGeometrica> clonarLista(List<FormaGeometrica> formas) {
        List<FormaGeometrica> copia = new ArrayList<>();
        if (formas != null) {
            for (FormaGeometrica forma : formas) {
                copia.add(forma.clonar());
            }
        }
        return copia;
    }

    /**
     * Registra o estado atual das formas na pilha de desfazer e limpa a pilha de refazer.
     */
    public void registrarEstado(List<FormaGeometrica> formas) {
        undoStack.push(clonarLista(formas));
        redoStack.clear();
    }

    /**
     * Desfaz a última ação realizada, restaurando o estado anterior.
     */
    public List<FormaGeometrica> desfazer(List<FormaGeometrica> estadoAtual) {
        if (!undoStack.isEmpty()) {
            redoStack.push(clonarLista(estadoAtual));
            List<FormaGeometrica> estadoAnterior = undoStack.pop();
            return clonarLista(estadoAnterior);
        }
        return clonarLista(estadoAtual);
    }

    /**
     * Refaz a ação previamente desfeita.
     */
    public List<FormaGeometrica> refazer(List<FormaGeometrica> estadoAtual) {
        if (!redoStack.isEmpty()) {
            undoStack.push(clonarLista(estadoAtual));
            List<FormaGeometrica> estadoProximo = redoStack.pop();
            return clonarLista(estadoProximo);
        }
        return clonarLista(estadoAtual);
    }

    public boolean podeDesfazer() {
        return !undoStack.isEmpty();
    }

    public boolean podeRefazer() {
        return !redoStack.isEmpty();
    }

    public void limpar() {
        undoStack.clear();
        redoStack.clear();
    }
}
