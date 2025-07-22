package org.example;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.example.bonus.Bonus;
import org.example.bonus.BonusDialog;
import org.example.wheel.MalusWheel;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class Main extends Application {

    public static final double SCENE_WIDTH = 1200;
    public static final double SCENE_HEIGHT = 900;
    public static final double WHEEL_RADIUS = 320;
    private static final Path SAVE_FILE = Path.of("loterie-save.txt");

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

        ObservableList<String> malus = FXCollections.observableArrayList(
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

        ObservableList<Bonus> bonusMaster = FXCollections.observableArrayList(
                new Bonus("Puissance +100"),
                new Bonus("Dommages ×1,2"),
                new Bonus("Initiative +500")
        );

        loadParticipants(users.getParticipants());

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
                Save.save(users.getParticipants());
                resultat.setMessage("État sauvegardé ✔");
            } catch (IOException ex) {
                resultat.setMessage("Erreur de sauvegarde ✖");
            }
        });

        Button clean = makeButton("Nettoyer", () -> {
            Save.reset(users.getParticipants());
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
        stage.show();
    }

    private static Button makeButton(String text, Runnable action) {
        Button b = new Button(text);
        Theme.styleButton(b);
        b.setOnAction(e -> action.run());
        return b;
    }

    private static void loadParticipants(ObservableList<Participant> list) {
        if (!Files.exists(SAVE_FILE)) return;
        try {
            Files.readAllLines(SAVE_FILE).stream()
                    .filter(l -> !l.isBlank() && !l.startsWith("#"))
                    .map(l -> l.split(";", -1))
                    .filter(p -> p.length >= 3)
                    .forEach(p -> {
                        Participant part = new Participant(p[0], Integer.parseInt(p[1]), p[2]);
                        if (p.length == 4 && !p[3].isBlank())
                            for (String b : p[3].split("\\|")) part.addBonus(new Bonus(b));
                        list.add(part);
                    });
        } catch (Exception ignored) { }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
