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
import org.example.bonus.Bonus;
import org.example.bonus.BonusDialog;
import org.example.wheel.MalusWheel;

import java.io.IOException;

public final class Main extends Application {

    public static final double SCENE_WIDTH = 1200;
    public static final double SCENE_HEIGHT = 900;
    public static final double WHEEL_RADIUS = 320;

    @Override
    public void start(Stage stage) {

        BorderPane root = new BorderPane();
        root.setBackground(Theme.makeBackgroundCover("/img.png"));

        Titre titre = new Titre();
        Resultat resultat = new Resultat();
        HBox top = new HBox(resultat.getNode());
        top.setAlignment(Pos.CENTER);
        root.setTop(new VBox(4, titre.getNode(), top));

        Users users = new Users();
        VBox left = new VBox(10, users.getRootPane());
        left.setPadding(new Insets(0, 10, 10, 20));
        left.setPrefSize(420, 820);
        root.setLeft(left);

        ObservableList<String> malus = FXCollections.observableArrayList();
        ObservableList<Bonus>  bonusMaster = FXCollections.observableArrayList();

        try {
            Save.load(users.getParticipants(), malus, bonusMaster);
        } catch (IOException ignored) { }

        if (malus.isEmpty()) {
            malus.addAll(
                    "Tu boites sévère (–2 PM)",
                    "Enragé (fin de tour au corps-à-corps)",
                    "Trop peureux (≥ 6 PO)",
                    "Panoplie imposée",
                    "Sorcier myope (≤ 3 PO)",
                    "Narcoleptique (skip 1 tour tous les 3 tours)",
                    "Écho étrange (1 sort/tour, toujours différent)",
                    "Oubli du familier",
                    "Aucun sort élémentaire (seulement neutres ou utilitaires)"
            );
        }

        if (bonusMaster.isEmpty()) {
            bonusMaster.addAll(
                    new Bonus("Puissance +100"),
                    new Bonus("Dommages ×1,2"),
                    new Bonus("Initiative +500")
            );
        }

        MalusPane malusPane = new MalusPane(malus);
        VBox right = new VBox(malusPane.getRootPane());
        right.setPadding(new Insets(0, 20, 10, 10));
        right.setPrefSize(460, 820);
        root.setRight(right);

        MalusWheel wheel = new MalusWheel(resultat);
        Historique historique = new Historique();
        wheel.setOnSpinFinished(historique::logResult);
        StackPane center = new StackPane(wheel.getRootPane());
        center.setAlignment(Pos.CENTER);
        center.setMaxSize(WHEEL_RADIUS * 2 + 50, WHEEL_RADIUS * 2 + 50);
        root.setCenter(center);

        malus.addListener((ListChangeListener<String>) c -> wheel.updateWheelDisplay(malus));
        wheel.updateWheelDisplay(malus);

        users.getParticipants().addListener(
                (ListChangeListener<Participant>) c -> wheel.updateWheelDisplay(malus));

        Button spin = makeButton("Lancer la roue !", () -> wheel.spinTheWheel(malus));
        spin.setFont(Font.font("Arial", 16));

        Button options = makeButton("Options...", () -> {
            new OptionRoue().showAndWait();
            wheel.updateWheelDisplay(malus);
        });

        Button reset = makeButton("Reset Position", wheel::resetPosition);

        Button save = makeButton("Sauvegarder état", () -> {
            try {
                Save.save(users.getParticipants(), malus, bonusMaster);
                resultat.setMessage("État sauvegardé ✔");
            } catch (IOException ex) {
                resultat.setMessage("Erreur de sauvegarde ✖");
            }
        });

        Button clean = makeButton("Nettoyer", () -> {
            Save.reset(users.getParticipants(), malus, bonusMaster);
            wheel.updateWheelDisplay(malus);
            resultat.setMessage("Nouvelle loterie prête");
        });

        Button indiv = makeButton("Roulette individuelle", () ->
                new BonusDialog(bonusMaster, users.getParticipants(), historique).show());

        Button fullScreen = makeButton("Plein écran", () -> stage.setFullScreen(!stage.isFullScreen()));

        Button history = makeButton("Historique...", historique::show);

        HBox bottom = new HBox(30, spin, options, reset, save, clean, indiv, fullScreen, history);
        bottom.setAlignment(Pos.CENTER);
        bottom.setPadding(new Insets(16, 0, 20, 0));
        root.setBottom(bottom);

        stage.setScene(new Scene(root, SCENE_WIDTH, SCENE_HEIGHT));
        stage.setTitle("Event PVP de la guilde EVOLUTION");
        stage.setMaximized(true);
        stage.show();
    }

    private static Button makeButton(String text, Runnable action) {
        Button b = new Button(text);
        Theme.styleButton(b);
        b.setOnAction(e -> action.run());
        return b;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
