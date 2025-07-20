package org.example;

import javafx.application.Application;
import javafx.collections.ListChangeListener;
import javafx.collections.FXCollections;
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


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;


public class Main extends Application {

    // Dimensions de la fenêtre
    public static final double SCENE_WIDTH   = 1200;
    public static final double SCENE_HEIGHT  = 900;

    // Rayon de la roue
    public static final double WHEEL_RADIUS  = 320;


    @Override
    public void start(Stage primaryStage) {
        // Root principal
        BorderPane root = new BorderPane();

        // === 1) Titre + Résultat (en haut) ===
        Titre bandeau = new Titre();
        Resultat resultat = new Resultat();

        // Rapprochés : spacing = 4 px
        HBox topBox = new HBox(resultat.getNode());
        topBox.setAlignment(Pos.CENTER);

        VBox topContainer = new VBox(4,
                bandeau.getNode(),
                topBox
        );
        topContainer.setAlignment(Pos.TOP_LEFT);
        root.setTop(topContainer);

        // Image de fond
        root.setBackground(Theme.makeBackgroundCover("/img.png"));

        // === 2) Participants (gauche) ===
        Users users = new Users();

        // Instancie la fenêtre des gains pour l'historique
        Gains gains = new Gains(users.getParticipants());
        Historique historique = new Historique();

        // liste partagée de tous les bonus disponibles
        ObservableList<Bonus> bonusMasterList = FXCollections.observableArrayList(
                new Bonus("Puissance +100"),
                new Bonus("Dommages ×1,2"),
                new Bonus("Initiative +500")
        );

        VBox leftBox = new VBox(10, users.getRootPane());
        // On supprime le padding-top
        leftBox.setPadding(new Insets(0, 10, 10, 20));
        leftBox.setAlignment(Pos.TOP_CENTER);

        // Agrandit la zone : 420 px large × 820 px haut
        leftBox.setPrefSize(420, 820);
        root.setLeft(leftBox);

        // === 3) Zone droite (vide pour le moment) ===
        VBox rightBox = new VBox();
        rightBox.setPadding(new Insets(0, 20, 10, 10));
        rightBox.setAlignment(Pos.TOP_CENTER);
        rightBox.setPrefSize(460, 820);
        root.setRight(rightBox);

        // --- Liste des malus ---
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
        rightBox.getChildren().add(malusPane.getRootPane());

        // === 4) Roue au centre ===
        Roue roue = new Roue(resultat);
        roue.setOnSpinFinished(historique::logResult);
        StackPane centerPane = new StackPane(roue.getRootPane());
        centerPane.setAlignment(Pos.CENTER);
        centerPane.setMaxSize(WHEEL_RADIUS * 2 + 50, WHEEL_RADIUS * 2 + 50);
        root.setCenter(centerPane);

        malusList.addListener((ListChangeListener<String>) c ->
                roue.updateWheelDisplay(malusList));

        // Recharge la sauvegarde, s’il y en a une
        try {
            Path f = Path.of("loterie-save.txt");
            if (Files.exists(f)) {
                var lines = Files.readAllLines(f);
                for (String line : lines) {
                    if (line.startsWith("#")) {
                        continue;
                    }
                    String[] parts = line.split(";", -1);     // -1 : garde le champ vide
                    if (parts.length >= 3) {
                        Participant p = new Participant(parts[0],
                                Integer.parseInt(parts[1]),
                                parts[2]);
                        if (parts.length == 4 && !parts[3].isBlank()) {
                            for (String b : parts[3].split("\\|"))
                                p.addBonus(new Bonus(b));
                        }
                        users.getParticipants().add(p);
                    }
                }
            }
        } catch (Exception ex) {
            System.err.println("Impossible de relire la sauvegarde : " + ex.getMessage());
        }

        // Mise à jour initiale de la roue
        roue.updateWheelDisplay(malusList);

        // Surveille les changements sur la liste de participants
        users.getParticipants().addListener(
                (ListChangeListener<Participant>) change -> {
                    roue.updateWheelDisplay(malusList);
                }
        );

        // === 5) Boutons en bas ===
        Button spinButton = new Button("Lancer la roue !");
        spinButton.setFont(Font.font("Arial", 16));
        spinButton.setOnAction(e -> {
            roue.spinTheWheel(malusList);   // ← correct
        });

        Button optionsButton = new Button("Options...");
        optionsButton.setOnAction(e -> {
            OptionRoue optWin = new OptionRoue();
            optWin.showAndWait();
            roue.updateWheelDisplay(malusList);
        });

        Button resetButton = new Button("Reset Position");
        resetButton.setOnAction(e -> roue.resetPosition());

        Button saveButton = new Button("Sauvegarder état");
        saveButton.setOnAction(e -> {
            try {
                Save.save(users.getParticipants());
                resultat.setMessage("État sauvegardé ✔");
            } catch (IOException ex) {
                resultat.setMessage("Erreur de sauvegarde ✖");
                ex.printStackTrace();
            }
        });

        Button cleanButton = new Button("Nettoyer");
        cleanButton.setOnAction(e -> {
            Save.reset(users.getParticipants());
            roue.updateWheelDisplay(malusList);
            resultat.setMessage("Nouvelle loterie prête");
        });

        Button indivButton = new Button("Roulette individuelle");
        Theme.styleButton(indivButton);
        indivButton.setOnAction(e ->
                new BonusDialog(bonusMasterList,
                                users.getParticipants(),
                                historique)
                        .show());

        // === Nouveau bouton "Plein écran" ===
        Button fullScreenButton = new Button("Plein écran");
        fullScreenButton.setOnAction(e -> {
            // Bascule l'état "fullscreen" à chaque clic
            boolean current = primaryStage.isFullScreen();
            primaryStage.setFullScreen(!current);
        });

        // Bouton pour afficher l'historique
        Button historyButton = new Button("Historique...");
        historyButton.setOnAction(e -> historique.show());

        // Style
        Theme.styleButton(spinButton);
        Theme.styleButton(optionsButton);
        Theme.styleButton(resetButton);
        Theme.styleButton(saveButton);
        Theme.styleButton(cleanButton);
        Theme.styleButton(indivButton);
        Theme.styleButton(fullScreenButton);
        Theme.styleButton(historyButton);

        HBox bottomBox = new HBox(30,
                spinButton, optionsButton, resetButton,
                saveButton, cleanButton, indivButton,
                fullScreenButton, historyButton
        );
        bottomBox.setAlignment(Pos.CENTER);
        bottomBox.setPadding(new Insets(16, 0, 20, 0));
        root.setBottom(bottomBox);

        // === 6) Scène + Stage ===
        Scene scene = new Scene(root, SCENE_WIDTH, SCENE_HEIGHT);
        primaryStage.setTitle("Event PVP de la guilde EVOLUTION");
        primaryStage.setScene(scene);

        // -> Optionnel : enlever l'indication pour quitter le fullscreen
        // primaryStage.setFullScreenExitHint("");

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}