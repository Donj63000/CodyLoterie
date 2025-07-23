package org.example.bonus;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.example.Historique;
import org.example.Participant;
import org.example.Resultat;
import org.example.Theme;
import org.example.wheel.BonusWheel;

public final class BonusDialog extends Stage {

    public BonusDialog(ObservableList<Bonus> bonusList,
                       ObservableList<Participant> players,
                       Historique historique) {

        setTitle("Roulette Bonus");
        initModality(Modality.APPLICATION_MODAL);

        Resultat resultat = new Resultat();
        BonusWheel wheel  = new BonusWheel(resultat);
        wheel.updateWheelDisplay(bonusList);

        ComboBox<Participant> combo = new ComboBox<>(players);
        combo.setPromptText("Joueur");
        combo.setConverter(new StringConverter<>() {
            @Override public String toString(Participant p) { return p == null ? "" : p.getName(); }
            @Override public Participant fromString(String s) { return null; }
        });

        Button spin = new Button("Lancer !");
        Theme.styleButton(spin);
        spin.disableProperty().bind(combo.getSelectionModel().selectedItemProperty().isNull());
        spin.setOnAction(e -> wheel.spinTheWheel(bonusList, combo.getValue()));

        ListView<Bonus> playerBonus = new ListView<>();
        Theme.styleListView(playerBonus);
        playerBonus.setPrefHeight(260);
        playerBonus.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Bonus item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item.toString());
                setStyle(empty ? "" : "-fx-font-size:18px;-fx-text-fill:#9be15d;");
            }
        });

        Button remove = new Button("Retirer");
        Theme.styleButton(remove);
        remove.disableProperty().bind(playerBonus.getSelectionModel().selectedItemProperty().isNull());
        remove.setOnAction(e -> {
            Bonus b = playerBonus.getSelectionModel().getSelectedItem();
            Participant p = combo.getValue();
            if (b != null && p != null) p.removeBonus(b);
        });

        combo.getSelectionModel().selectedItemProperty().addListener((o, ov, nv) ->
                playerBonus.setItems(nv == null ? FXCollections.emptyObservableList() : nv.getBonusList()));

        wheel.setOnBonusWon((p, b) -> {
            p.addBonus(new Bonus(b));
            historique.logBonus(p, b);
        });

        HBox bottom = new HBox(10, combo, spin);
        bottom.setAlignment(Pos.CENTER);
        bottom.setPadding(new Insets(10));

        VBox right = new VBox(10, playerBonus, remove);
        right.setAlignment(Pos.TOP_CENTER);
        right.setPadding(new Insets(10));

        BorderPane root = new BorderPane();
        root.setLeft(new BonusPane(bonusList).getRootPane());
        root.setCenter(wheel.getRootPane());
        root.setRight(right);
        root.setTop(resultat.getNode());
        BorderPane.setAlignment(resultat.getNode(), Pos.CENTER);
        root.setBottom(bottom);
        root.setBackground(Theme.makeBackgroundCover("/img_1.png"));

        setScene(new Scene(root, 800, 600));
        setMaximized(true);
    }
}
