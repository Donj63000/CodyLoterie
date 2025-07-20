package org.example.bonus;

import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.example.*;

public class BonusDialog extends Stage {

    public BonusDialog(ObservableList<Bonus> bonusList,
                       ObservableList<Participant> players,
                       Historique historique) {
        setTitle("Roulette Bonus");
        initModality(Modality.APPLICATION_MODAL);

        Resultat result = new Resultat();
        RouletteBonus wheel = new RouletteBonus(result);
        BonusPane pane = new BonusPane(bonusList);

        ComboBox<Participant> combo = new ComboBox<>(players);
        combo.setPromptText("Joueur");
        combo.setConverter(new StringConverter<>() {
            @Override public String toString(Participant p){ return p==null?"":p.getName(); }
            @Override public Participant fromString(String s){ return null; }
        });

        Button spin = new Button("Lancer !");
        Theme.styleButton(spin);
        spin.setOnAction(e -> {
            Participant p = combo.getSelectionModel().getSelectedItem();
            if (p == null) { Theme.showError("Choisir un joueur"); return; }
            wheel.spinTheWheel(bonusList, p);
        });

        // 1) list view des bonus du joueur
        ListView<Bonus> playerBonus = new ListView<>();
        Theme.styleListView(playerBonus);
        playerBonus.setPrefHeight(260);

        // 2) bouton pour retirer le bonus sélectionné
        Button remove = new Button("Retirer");
        Theme.styleButton(remove);
        remove.setOnAction(ev -> {
            Bonus b = playerBonus.getSelectionModel().getSelectedItem();
            Participant p = combo.getSelectionModel().getSelectedItem();
            if (b!=null && p!=null) p.removeBonus(b);
        });

        // 3) chaque fois qu’on change de joueur -> montre ses bonus
        combo.getSelectionModel().selectedItemProperty().addListener((obs, oldP, newP) -> {
            playerBonus.setItems(newP==null? FXCollections.emptyObservableList()
                                            : newP.getBonusList());
        });

        wheel.setOnBonusWon((p,b) -> {
            p.addBonus(new Bonus(b));
            historique.logBonus(p,b);
        });

        HBox bottom = new HBox(10, combo, spin);
        bottom.setAlignment(Pos.CENTER);
        bottom.setPadding(new Insets(10));

        BorderPane root = new BorderPane();
        root.setLeft(pane.getRootPane());
        root.setCenter(wheel.getRootPane());

        VBox right = new VBox(10, playerBonus, remove);
        right.setAlignment(Pos.TOP_CENTER);
        right.setPadding(new Insets(10));

        root.setRight(right);
        root.setTop(result.getNode());
        BorderPane.setAlignment(result.getNode(), Pos.CENTER);
        root.setBottom(bottom);

        Scene scene = new Scene(root, 800, 600);
        setScene(scene);
    }
}
