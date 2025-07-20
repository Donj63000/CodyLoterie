package org.example;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main extends Application {

    public static final double SCENE_WIDTH = 1200;
    public static final double SCENE_HEIGHT = 900;
    public static final double WHEEL_RADIUS = 320;

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();

        Titre titre = new Titre();
        Resultat resultat = new Resultat();
        HBox topBox = new HBox(resultat.getNode());
        topBox.setAlignment(Pos.CENTER);
        VBox top = new VBox(4, titre.getNode(), topBox);
        top.setAlignment(Pos.TOP_LEFT);
        root.setTop(top);
        root.setBackground(Theme.makeBackgroundCover("/img.png"));

        Users users = new Users();
        Historique historique = new Historique();

        VBox left = new VBox(10, users.getRootPane());
        left.setPadding(new Insets(0, 10, 10, 20));
        left.setAlignment(Pos.TOP_CENTER);
        left.setPrefSize(420, 820);
        root.setLeft(left);

        VBox right = new VBox();
        right.setPadding(new Insets(0, 20, 10, 10));
        right.setAlignment(Pos.TOP_CENTER);
        right.setPrefSize(460, 820);
        root.setRight(right);

        ObservableList<String> malusList = FXCollections.observableArrayList(
                "Tu boites sévère (–2\u202FPM)",
                "Enragé (fin de tour au corps-à-corps)",
                "Trop peureux (\u2265\u202F6\u202FPO)",
                "Panoplie imposée",
                "Sorcier myope (\u2264\u202F3\u202FPO)",
                "Narcoleptique (skip 1 tour tous les 3 tours)",
                "Écho étrange (1 sort/tour, toujours différent)",
                "Oubli du familier",
                "Aucun sort élémentaire (seulement neutres ou utilitaires)"
        );

        MalusPane malusPane = new MalusPane(malusList);
        right.getChildren().add(malusPane.getRootPane());

        Roue roue = new Roue(resultat);
        roue.setOnSpinFinished(historique::logResult);
        StackPane center = new StackPane(roue.getRootPane());
        center.setAlignment(Pos.CENTER);
        center.setMaxSize(WHEEL_RADIUS * 2 + 50, WHEEL_RADIUS * 2 + 50);
        root.setCenter(center);

        malusList.addListener((ListChangeListener<String>) c -> roue.updateWheelDisplay(malusList));
        loadSave(users);
        roue.updateWheelDisplay(malusList);
        users.getParticipants().addListener((ListChangeListener<Participant>) c -> roue.updateWheelDisplay(malusList));

        Button spin = makeButton("Lancer la roue !", roue::spinTheWheel);
        Button options = makeButton("Options...", () -> {
            new OptionRoue().showAndWait();
            roue.updateWheelDisplay(malusList);
        });
        Button reset = makeButton("Reset Position", roue::resetPosition);
        Button save = makeButton("Sauvegarder état", () -> {
            try {
                Save.save(users.getParticipants());
                resultat.setMessage("État sauvegardé ✔");
            } catch (IOException e) {
                resultat.setMessage("Erreur de sauvegarde ✖");
            }
        });
        Button clean = makeButton("Nettoyer", () -> {
            Save.reset(users.getParticipants());
            roue.updateWheelDisplay(malusList);
            resultat.setMessage("Nouvelle loterie prête");
        });
        Button fullscreen = makeButton("Plein écran", () -> primaryStage.setFullScreen(!primaryStage.isFullScreen()));
        Button history = makeButton("Historique...", historique::show);

        HBox bottom = new HBox(30, spin, options, reset, save, clean, fullscreen, history);
        bottom.setAlignment(Pos.CENTER);
        bottom.setPadding(new Insets(16, 0, 20, 0));
        root.setBottom(bottom);

        Scene scene = new Scene(root, SCENE_WIDTH, SCENE_HEIGHT);
        primaryStage.setTitle("Event PVP de la guilde EVOLUTION");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private static Button makeButton(String text, Runnable action) {
        Button b = new Button(text);
        b.setFont(Font.font("Arial", 16));
        b.setOnAction(e -> action.run());
        Theme.styleButton(b);
        return b;
    }

    private static void loadSave(Users users) {
        Path f = Path.of("loterie-save.txt");
        if (!Files.exists(f)) return;
        try {
            for (String line : Files.readAllLines(f)) {
                if (line.startsWith("#")) continue;
                String[] p = line.split(";", 3);
                if (p.length == 3) users.getParticipants().add(new Participant(p[0], Integer.parseInt(p[1]), p[2]));
            }
        } catch (Exception ignored) { }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
