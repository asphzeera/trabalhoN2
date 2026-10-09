package org.example;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.Views.ContainerApp;

/**
 * Ponto de entrada da aplicação JavaFX para o Editor de Desenho Vetorial Interativo.
 */
public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        ContainerApp containerApp = new ContainerApp(primaryStage);
        containerApp.setStagePrincipal(primaryStage);

        Scene scene = new Scene(containerApp, 1280, 800);

        primaryStage.setTitle("Editor de Desenho Vetorial Interativo - UniRV POO 1");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
