package org.example.Controller;

import javafx.scene.input.MouseEvent;

/**
 * Interface do padrão State para definir o comportamento das ferramentas interativas no canvas.
 */
public interface Ferramenta {
    void aoPressionarMouse(MouseEvent event, EditorContext context);
    void aoArrastarMouse(MouseEvent event, EditorContext context);
    void aoSoltarMouse(MouseEvent event, EditorContext context);
    void aoClicar(MouseEvent event, EditorContext context);
}
